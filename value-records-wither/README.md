# value-records-wither

A tiny annotation processor that generates fluent `withX(...)` copy methods for
`record`s — including Java 28 `value record`s.

## Why

Records are immutable, so "change one field" means "construct a new one, copying the
rest". By hand that is noisy and easy to get wrong. This processor generates it.

## Usage

Annotate the record and have it implement the generated `<Record>With` interface:

```java
@GenerateWithers
public value record Owner(OwnerId id, String name) implements OwnerWith { }

Owner renamed = owner.withName("Dibbert");   // new Owner, id copied
```

An annotation processor can only *create* files, never edit the record, so the
`implements OwnerWith` opt-in is required. The reference to the not-yet-generated
interface resolves across annotation-processing rounds.

### Generated code

```java
public interface OwnerWith {
    OwnerId id();
    String name();
    default Owner withId(OwnerId id)    { return new Owner(id, name()); }
    default Owner withName(String name) { return new Owner(id(), name); }
}
```

## Build

Standalone (not part of the app build):

```bash
./mvnw -f value-records-wither/pom.xml test
```

## Wiring it into the PetClinic app (optional)

1. `./mvnw -f value-records-wither/pom.xml install` (publishes the jar to `~/.m2`).
2. In the app `pom.xml`, add it to the compiler's annotation-processor path and as a
   compile dependency for the annotation:

   ```xml
   <dependency>
       <groupId>com.dominikdorn.zazr</groupId>
       <artifactId>value-records-wither</artifactId>
       <version>0.1.0-SNAPSHOT</version>
   </dependency>
   ```
   ```xml
   <plugin>
       <artifactId>maven-compiler-plugin</artifactId>
       <configuration>
           <annotationProcessorPaths>
               <path>
                   <groupId>com.dominikdorn.zazr</groupId>
                   <artifactId>value-records-wither</artifactId>
                   <version>0.1.0-SNAPSHOT</version>
               </path>
           </annotationProcessorPaths>
       </configuration>
   </plugin>
   ```
3. Annotate a record, e.g. `@GenerateWithers value record OwnerId(int id) implements OwnerIdWith`.
