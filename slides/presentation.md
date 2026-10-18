theme: Huerta
footer: Functional PetClinic — errors & absence as values — Java Vienna 2026-10-19 — Dominik Dorn
slidenumbers: true

# Functional PetClinic
## Making illegal states unrepresentable with Java 28 & zazr

### Java Vienna — Dominik Dorn

^ Welcome! Tonight we take the most boring app in the Java world — Spring PetClinic — and rebuild its core on two ideas: errors are values, and absence is a value. No `null`, no exceptions for control flow. The twist is doing it inside real Spring MVC, JPA and Jackson, on Java 28. I'll keep slides light and spend the time in the code.

---

![right](me.jpg)

# About Me

## Dominik Dorn

- Software Developer
- Java enthusiast since 2006
- Running Java Vienna since 2009
- Freelancer, interested about new projects
- blog: https://dominikdorn.com
- busy with my biggest "side project"
- X: @domdorn

---

# Agenda

1. The problem: `null` and exceptions in PetClinic
2. Absence as a value — `Option`
3. Errors as values — `Either`
4. Typed IDs as `value record` (and the JPA wall)
5. The payoff: a report built from `zip`
6. What it costs — gotchas & verdict

^ Six beats. The middle three are the heart. The last one is the honest part — this isn't free, and I'll show you exactly where Spring/JPA/Jackson fought back.

---

# The setup

- Classic **Spring PetClinic**, reworked
- **Java 28** (EA): value classes & records, `--enable-preview`
- **Spring Boot 4.1**, Hibernate 7, Jackson 3
- **zazr** — a `vavr` fork: `Option`, `Either`, `Validation`, `Vector`
- Everything in a git history you can `git log` through

^ zazr in one line: if you know vavr or Scala's standard library, you know zazr. Option = Optional-done-right, Either = a result that is one of two things, Validation = Either that accumulates errors. That's 90% of what you need tonight.

---

# The problem

## Two ways PetClinic lies to you

- `Owner owner = repo.findById(id);` → **`null`**, waiting to NPE
- "not found" → **throw**, 500 to the user
- A missing birth date → **`null` field**, `NullPointerException` in the view
- The type says `Owner`. The truth says *maybe* `Owner`.

^ The signature `Owner findById(int)` is a lie: it might not return an Owner. And when the owner isn't there, the classic app either NPEs or throws an exception you then have to remember to catch and translate. The type system knows nothing. Our whole talk is about making the type tell the truth.

---

# Beat 1 — Absence as a value

## `null` → `Option<T>`

```java
// before: null leaks everywhere
public Owner findOwner(Integer id) {
    return id == null ? new Owner()
                      : repo.findById(id);   // may be null!
}
```

```java
// after: the Option can't be ignored
public Owner findOwner(Option<OwnerId> ownerId) {
    return ownerId.flatMap(owners::find)     // Option<Owner>
                  .getOrElse(Owner::new);
}
```

^ `owners.find` returns `Option<Owner>`, not a nullable Owner. `flatMap` only runs if the id is present; `getOrElse` forces me to say what happens when it's absent. There is no code path where I forget. Note the parameter is already `Option<OwnerId>` — more on that in a second.

---

# Absence all the way down

## `Pet.birthDate` is honestly optional

```java
private Option<LocalDate> birthDate = Option.none();
```

- Persisted through a generic JPA `AttributeConverter`
- One converter, `autoApply` — no per-field annotations

```java
@Converter(autoApply = true)
public class OptionLocalDateConverter
        extends OptionConverter<LocalDate> { }
```

^ The field type itself says "this may be absent." JPA doesn't understand `Option`, so we taught it: a generic `OptionConverter<T>` that maps `Option<T>` <-> `T` column, auto-applied so nobody has to annotate every optional field. This is the first place the framework had to be bent to our will — and it was ~15 lines.

---

# `@PathVariable Option<OwnerId>`

## even the HTTP boundary speaks `Option`

```java
@ModelAttribute("owner")
public Owner findOwner(
    @PathVariable(name = "ownerId", required = false)
    Option<OwnerId> ownerId) { ... }
```

- `/owners/new` → `Option.none()` (no id in the path)
- `/owners/42` → `Option.some(OwnerId(42))`
- A custom `HandlerMethodArgumentResolver` does it — never `null`

^ Spring's built-in resolver hands you `null` for a missing optional path variable. We prepend our own resolver so the controller gets a real empty `Option` instead. The point: absence is modelled at the very edge of the system, so `null` never even enters.

---

# Beat 2 — Errors as values

## the centerpiece: `Either<Error, Result>`

```java
@GetMapping("/owners/{ownerId}")
public Either<DomainError, ModelAndView> showOwner(int ownerId) {
    OwnerId id = new OwnerId(ownerId);
    return owners.find(id)
        .toEither(() -> new OwnerNotFound(id))   // Option -> Either
        .map(owner -> new ModelAndView("owners/ownerDetails")
                          .addObject(owner));
}
```

- `Right` → the view. `Left` → an `OwnerNotFound` **value**
- No `throw`, no `@ExceptionHandler`, no 500

^ Read it top to bottom: find the owner; if absent, the Left is a not-found *value* carrying the id; if present, map to a view. The error is just data flowing through. `OwnerNotFound` is a `value record` implementing a sealed `NotFound` interface. The controller never throws.

---

# before / after — the whole point

```java
// BEFORE — imperative, exceptions, null
@GetMapping("/owners/{id}")
public ModelAndView show(@PathVariable int id) {
    Owner o = repo.findById(id);
    if (o == null)
        throw new NotFoundException(id);   // -> 500 unless caught
    return new ModelAndView("ownerDetails").addObject(o);
}
```

```java
// AFTER — the result is a value you can't ignore
@GetMapping("/owners/{id}")
public Either<DomainError, ModelAndView> show(int id) {
    return owners.find(new OwnerId(id))
        .toEither(() -> new OwnerNotFound(new OwnerId(id)))
        .map(o -> new ModelAndView("ownerDetails").addObject(o));
}
```

^ This is the slide I want you to remember. Same endpoint. Top: the error is invisible in the type and escapes as an exception. Bottom: the error is in the return type; the compiler and the reader both see it. Everything else tonight is in service of making the bottom version actually work in Spring.

---

# Who renders the `Either`?

## one small handler bridges zazr → Spring MVC

- `EitherReturnValueHandler` (~40 lines) teaches MVC to unwrap `Either`
- **View endpoints:** `Right` → view, `Left` → status + error view
- **JSON endpoints:** `Right` → body, `Left` → **RFC-7807 problem**
- The app supplies the `Left → status` mapping via an SPI

```java
case NotFound        -> 404;
case ValidationFailed -> 400;
default              -> 500;
```

^ The handler itself knows nothing about *our* errors — it delegates the "what status / what body" decision to an application-provided strategy. So the reusable glue lives in a `com.dominikdorn.zazr` namespace, and the PetClinic-specific error taxonomy stays in the app. This is the seam that makes the whole approach shippable.

---

# Demo 1 — errors as values

## `/owners/999/summary`

- Same `Either` style, but `@ResponseBody`
- `Right` → `{"id":1,"lastName":"Franklin", ...}`
- `Left` → `404` **`application/problem+json`**

```json
{ "title": "Not Found", "status": 404,
  "detail": "Owner not found with id: 999",
  "instance": "/owners/999/summary" }
```

^ LIVE: curl the summary for a real owner (200 + JSON), then for 999 (404 + problem detail). Same controller shape, HTML path renders an error page, REST path renders RFC-7807 — and the controller code didn't change. ~2 minutes, have the terminal ready.

---

# Beat 3 — Typed IDs

## stop passing `int` around

```java
public value record OwnerId(int id) implements Serializable { }
public value record PetId(int id)   implements Serializable { }
```

- `owners.find(petId)` → **won't compile**. Good.
- Java 28 `value record`: identity-free, no heap games
- Repository is keyed by the typed id, not `Integer`

^ A bare `int` id is another lie — nothing stops you passing a PetId where an OwnerId belongs. Value records make each id its own type for free, with value semantics. This is where Java 28 earns its place in the talk.

---

# The JPA wall

## typed `@Id` is where reality bites

- `AttributeConverter` is **forbidden** on `@Id`
- So: a Hibernate `EnhancedUserType<OwnerId>`
- Postgres multi-key loads needed `@Fetch(SUBSELECT)`
- `toString()` had to return the int — Thymeleaf builds URLs from it!

```java
@Id @GeneratedValue(IDENTITY)
@Type(OwnerIdType.class)
private OwnerId id;
```

^ This is the credibility beat — the part a lot of "functional Spring" talks skip. You can't just convert an id type on the `@Id`; Hibernate says no. You implement a UserType. And then a demo-breaking surprise: Thymeleaf renders `${owner.id}` into links, which gave `OwnerId[id=1]` in the URL until I overrode `toString`. I caught it with a rendering test. Real integration has sharp edges.

---

# Beat 4 — Validation as values

## accumulate, don't short-circuit

```java
Validation<Violation, Pet> checked =
    Validation.zipWith(hasBirthDate, hasBeenSeen, noFutureVisit,
                       (a, b, c) -> pet);

return checked.fold(NonEmptyVector::toVector,   // all failures
                    ok -> Vector.empty());
```

- `Either` stops at the **first** error
- `Validation` collects **every** violation at once
- Same `Violation` values bridge into Spring's `BindingResult`

^ Validation is Either's sibling: where Either short-circuits, Validation's `zipWith` runs all the checks and gathers every violation. Perfect for forms and data-quality reports. And the same violation values feed Spring's BindingResult, so server-side form errors still work the classic way.

---

# Beat 5 — The payoff

## a report that reads like its spec: `zip`

```java
// month-over-month deltas: pair each month with the previous one
series.zip(series.drop(1))
      .map(pair -> point(pair._2(), Option.some(pair._1())));
```

```java
// the imperative version you DON'T write anymore
for (int i = 1; i < months.size(); i++) {
    var prev = months.get(i - 1);
    var cur  = months.get(i);
    deltas.add(cur.count() - prev.count());
}
```

^ Zipping a sequence with its own tail gives you consecutive pairs — a sliding window with no indices and no mutable `prev`. The first month has no predecessor, so its delta is `Option.none()`, rendered as JSON `null`, never a fake zero. This is the slide where functional stops being academic and starts being obviously nicer.

---

# Demo 2 — the report + XHR UI

## `/reports` and `/owners/{id}/report`

- `groupMapReduce` for the monthly series & per-type counts
- `Option` for "no previous month", "no birth date", "never seen"
- Data-quality warnings accumulated via `Validation`
- UI: a **text-free** page; `fetch()` renders everything client-side

^ LIVE: open /reports — the trend table with deltas and the type bars, the amber warnings strip (future-dated visits, gaps in the month series). Then an owner page's "Care report" panel. All of it is one `Either` JSON endpoint rendered by the handler from beat 2, with zazr-jackson serializing `Vector`/`Option` straight to JSON. ~4 minutes.

---

# Bonus — `withX` for value records

## records are immutable, so "change one field" means rebuild

```java
@GenerateWithers
public value record TrendPoint(String month, long visits,
        Option<Long> delta, Option<Double> changePct)
    implements TrendPointWith { }          // <- interface is generated
```

```java
// generated TrendPointWith — one default per component
default TrendPoint withDelta(Option<Long> delta) {
    return new TrendPoint(month(), visits(), delta, changePct());
}
```

- Used in the trend: `bare(current).withDelta(d).withChangePct(p)`
- A ~90-line annotation processor; the record opts in with `implements`

^ Bonus beat — skip if time is short. It ties the talk together: these really are value records, and the trend code two slides back used withDelta/withChangePct. A processor can only CREATE files, never edit the record — so the record implements a generated interface (same trick RecordBuilder uses). It lives in its own artifact because javac discovers processors at the very start of a compile. ~2 minutes.

---

# What it costs

## the honest slide (from `gotchas.md`)

- `AttributeConverter` forbidden on `@Id` → `EnhancedUserType`
- Typed-id `toString()` broke Thymeleaf URLs *(demo-critical!)*
- `Vector.tail()` throws on empty → use `drop(1)`
- Mockito returns `null`, not `Option.none()`, for unstubbed stubs
- `@ModelAttribute` runs before validation — ordering surprises
- IDE has no Java 28 support yet — compile to check

^ I kept a gotchas file the whole way. These are the real costs. None are dealbreakers, but anyone selling you "functional Spring" without this list is selling you something. The toString one genuinely would have blown up the live demo.

---

# Verdict

## would I ship this?

- **Yes** for the boundary: `Option` fields, `Either` endpoints, problem+json
- **Yes** for typed IDs — cheap safety, Java 28 makes them free
- **Maybe** for full value-everywhere — tooling (IDE, EA JDK) isn't there *yet*
- The payoff: **the type system stops lying to you**

^ My honest take: the error/absence-at-the-boundary pattern is a clear win today and you could adopt just that. Value classes everywhere is a 2027 conversation once tooling catches up. But the direction is right: every lie we removed from a signature was a class of bug removed with it.

---

# Thank you!

## Functional PetClinic

- Code & full `git log`: *<repo link>*
- `zazr`: https://zazr.dev
- blog: https://dominikdorn.com · X: @domdorn

### Questions?

^ Happy to go deeper in Q&A on the EitherReturnValueHandler internals, the Hibernate UserType, or why I did/didn't extract a separate Maven module. Backup slides follow.

---

# Backup — the handler, in full

```java
public void handleReturnValue(Object value, MethodParameter type, ...) {
    Either<?, ?> either = (Either<?, ?>) value;
    if (writesBody(type)) {            // @ResponseBody / REST
        switch (either) {
            case Right(var v) -> bodyProcessor.handleReturnValue(v, ...);
            case Left(var e)  -> { var r = leftHandler.render(e);
                                   response.setStatus(r.status());
                                   bodyProcessor.handleReturnValue(r.body(), ...); }
        }
    } else { /* view: Right -> view name, Left -> status + error view */ }
}
```

^ Pattern matching on the sealed `Either` (Right/Left records). The `leftHandler` is the app-supplied SPI. ~40 lines total including the view branch.

---

# Backup — package structure

```
owner/              entities, value-record ids, id-types, *NotFound
owner/persistence/  repositories
owner/web/          controllers, converters, validators
report/             the reporting feature
functional/         DomainError, Violation, Validation bridge
com.dominikdorn.zazr.{web,jpa}   reusable glue
```

^ Domain in the root, layers in subpackages, reusable framework glue in its own namespace so it could become a library later.

---

# Backup — zazr cheat sheet

- `Option<T>` — `some` / `none`, `map` / `flatMap` / `getOrElse`
- `Either<L, R>` — `left` / `right`, `map`, `toEither`, sealed (pattern match)
- `Validation<E, A>` — `valid` / `invalid`, `zipWith` (accumulates!)
- `Vector<T>` — immutable sequence: `zip`, `groupMapReduce`, `drop`, `fold`
- `NonEmptyVector<T>` — a `Vector` the type system knows isn't empty

^ Keep this up during Q&A so API names aren't a mystery.
