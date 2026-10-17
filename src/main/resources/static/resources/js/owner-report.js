// Loads the per-owner care report as JSON and renders it into the owner details page.
// The endpoint URL is derived from the current path (/owners/{id} -> /owners/{id}/report)
// so it naturally respects any context path.
(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", function () {
    text("care-report-heading", "Care report");
    text("owner-report-status", "Loading report…");

    var url = window.location.pathname.replace(/\/+$/, "") + "/report";
    fetch(url, { headers: { Accept: "application/json" } })
      .then(function (response) {
        if (!response.ok) {
          // Either's Left -> RFC-7807 problem+json.
          return response.json().then(function (problem) {
            throw new Error(problem.detail || problem.title || ("HTTP " + response.status));
          });
        }
        return response.json();
      })
      .then(render)
      .catch(function (error) {
        text("owner-report-status", "Could not load report: " + error.message);
      });
  });

  function render(report) {
    document.getElementById("owner-report-status").style.display = "none";
    renderWarnings(report.warnings || []);
    renderPets(report.pets || []);
  }

  function renderWarnings(warnings) {
    if (warnings.length === 0) {
      return;
    }
    var alert = document.createElement("div");
    alert.className = "alert alert-warning";
    var heading = document.createElement("strong");
    heading.textContent = "Data-quality warnings";
    alert.appendChild(heading);
    var list = document.createElement("ul");
    list.style.marginBottom = "0";
    warnings.forEach(function (warning) {
      var item = document.createElement("li");
      item.textContent = warning.message;
      list.appendChild(item);
    });
    alert.appendChild(list);
    document.getElementById("owner-report").appendChild(alert);
  }

  function renderPets(pets) {
    var table = document.createElement("table");
    table.className = "table table-striped";

    var head = table.createTHead().insertRow();
    ["Pet", "Type", "Age", "Last visit", "Visits"].forEach(function (label) {
      var th = document.createElement("th");
      th.textContent = label;
      head.appendChild(th);
    });

    var body = table.createTBody();
    pets.forEach(function (pet) {
      var row = body.insertRow();
      row.insertCell().textContent = pet.name;
      row.insertCell().textContent = pet.type;
      // age/lastVisit are zazr Options -> JSON null when absent: show that honestly.
      row.insertCell().textContent = pet.age === null || pet.age === undefined ? "—" : pet.age + " yr";
      row.insertCell().textContent = pet.lastVisit === null || pet.lastVisit === undefined ? "never" : pet.lastVisit;
      row.insertCell().textContent = pet.visits;
    });

    document.getElementById("owner-report").appendChild(table);
  }

  function text(id, value) {
    document.getElementById(id).textContent = value;
  }
})();
