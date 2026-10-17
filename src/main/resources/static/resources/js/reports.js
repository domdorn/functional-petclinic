// Fetches the visit-trends report as JSON and renders it. Deliberately framework-free:
// one fetch(), a little DOM building, no server-side templating of the data.
(function () {
  "use strict";

  var ENDPOINT = "api/reports/visit-trends";

  document.addEventListener("DOMContentLoaded", function () {
    // All visible text lives here, not in the template, so the page carries no
    // server-rendered copy of the data (and no hard-coded HTML strings).
    text("report-heading", "Visit trends");
    text("report-subtitle", "Loaded over XHR from /api/reports/visit-trends — no server-side templating of the data.");
    text("report-status", "Loading report…");
    headerRow("trend-head", ["Month", "Visits", "", "Δ vs prev.", "Change"]);
    headerRow("type-head", ["Type", "Visits", ""]);

    fetch(ENDPOINT, { headers: { Accept: "application/json" } })
      .then(function (response) {
        if (!response.ok) {
          // The Either's Left is rendered as an RFC-7807 problem+json body.
          return response.json().then(function (problem) {
            throw new Error(problem.detail || problem.title || ("HTTP " + response.status));
          });
        }
        return response.json();
      })
      .then(render)
      .catch(function (error) {
        document.getElementById("report-status").textContent = "Could not load report: " + error.message;
      });
  });

  function render(report) {
    text("trend-title", "Monthly visits");
    text("type-title", "By pet type");
    renderWarnings(report.warnings || []);
    renderTrend(report.series || []);
    renderByType(report.byType || []);

    document.getElementById("report-status").style.display = "none";
    document.getElementById("report").style.display = "block";
  }

  function text(id, value) {
    document.getElementById(id).textContent = value;
  }

  function headerRow(id, labels) {
    var row = document.getElementById(id);
    labels.forEach(function (label) {
      var cell = document.createElement("th");
      cell.textContent = label;
      row.appendChild(cell);
    });
  }

  function renderWarnings(warnings) {
    if (warnings.length === 0) {
      return;
    }
    var box = document.getElementById("warnings");
    var list = document.createElement("ul");
    warnings.forEach(function (warning) {
      var item = document.createElement("li");
      item.textContent = warning.message;
      list.appendChild(item);
    });
    var heading = document.createElement("strong");
    heading.textContent = "Data-quality warnings";
    box.appendChild(heading);
    box.appendChild(list);
    box.style.display = "block";
  }

  function renderTrend(series) {
    var max = series.reduce(function (acc, point) { return Math.max(acc, point.visits); }, 0);
    var body = document.querySelector("#trend-table tbody");
    series.forEach(function (point) {
      var row = body.insertRow();
      row.insertCell().textContent = point.month;
      row.insertCell().textContent = point.visits;
      bar(row.insertCell(), point.visits, max);
      deltaCell(row.insertCell(), point.delta);
      pctCell(row.insertCell(), point.changePct);
    });
  }

  function renderByType(byType) {
    var max = byType.reduce(function (acc, entry) { return Math.max(acc, entry.visits); }, 0);
    var body = document.querySelector("#type-table tbody");
    byType.forEach(function (entry) {
      var row = body.insertRow();
      row.insertCell().textContent = entry.type;
      row.insertCell().textContent = entry.visits;
      bar(row.insertCell(), entry.visits, max);
    });
  }

  function bar(cell, value, max) {
    var width = max > 0 ? Math.round((value / max) * 160) : 0;
    var span = document.createElement("span");
    span.className = "report-bar";
    span.style.width = width + "px";
    cell.appendChild(span);
  }

  // delta is null for the first month (zazr Option.none -> JSON null): the absence is
  // modelled, so we show an em dash rather than a misleading 0.
  function deltaCell(cell, delta) {
    cell.className = "text-end";
    if (delta === null || delta === undefined) {
      cell.classList.add("report-delta-none");
      cell.textContent = "—";
      return;
    }
    cell.classList.add(delta > 0 ? "report-delta-up" : delta < 0 ? "report-delta-down" : "report-delta-none");
    cell.textContent = (delta > 0 ? "+" : "") + delta;
  }

  function pctCell(cell, changePct) {
    cell.className = "text-end";
    if (changePct === null || changePct === undefined) {
      cell.classList.add("report-delta-none");
      cell.textContent = "—";
      return;
    }
    cell.classList.add(changePct > 0 ? "report-delta-up" : changePct < 0 ? "report-delta-down" : "report-delta-none");
    cell.textContent = (changePct > 0 ? "+" : "") + changePct.toFixed(1) + "%";
  }
})();
