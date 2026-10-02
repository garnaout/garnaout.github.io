/* The retention model in the hero.
   Compounds a starting customer base at two net-retention rates and draws both. */
(function () {
  "use strict";

  var root = document.getElementById("model");
  var chart = document.getElementById("model-chart");
  var slider = document.getElementById("model-nrr");
  if (!root || !chart || !slider) return;

  var BASE = Number(root.dataset.base) || 10; // starting ARR, $M
  var BENCH = Number(root.dataset.benchmark) || 136; // comparison net retention, %
  var YEARS = Number(root.dataset.years) || 5;
  var COLOR_YOU = "var(--chart-you)";
  var COLOR_BENCH = "var(--chart-bench)";
  var SVG_NS = "http://www.w3.org/2000/svg";

  var W = 420, H = 250;
  var M = { top: 12, right: 58, bottom: 28, left: 42 };
  var plotW = W - M.left - M.right;
  var plotH = H - M.top - M.bottom;
  var Y_MAX = Math.ceil((BASE * Math.pow(BENCH / 100, YEARS)) / 10) * 10;

  function series(nrr) {
    var out = [];
    for (var y = 0; y <= YEARS; y++) out.push(BASE * Math.pow(nrr / 100, y));
    return out;
  }
  function x(year) { return M.left + (year / YEARS) * plotW; }
  function y(value) { return M.top + plotH - (Math.min(value, Y_MAX) / Y_MAX) * plotH; }
  function money(value) { return "$" + value.toFixed(1) + "M"; }
  function path(values) {
    return values.map(function (v, i) {
      return (i ? "L" : "M") + x(i).toFixed(1) + " " + y(v).toFixed(1);
    }).join(" ");
  }
  function el(name, attrs, text) {
    var node = document.createElementNS(SVG_NS, name);
    for (var key in attrs) {
      /* colors go through style so they can use the stylesheet's variables */
      if (key === "fill" || key === "stroke") node.style[key] = attrs[key];
      else node.setAttribute(key, attrs[key]);
    }
    if (text != null) node.textContent = text;
    return node;
  }
  function html(name, className, text) {
    var node = document.createElement(name);
    if (className) node.className = className;
    if (text != null) node.textContent = text;
    return node;
  }

  var bench = series(BENCH);
  var you = series(Number(slider.value));

  /* Legend */
  var legend = html("div", "model__legend");
  var legendYou = html("span");
  var keyYou = html("i");
  keyYou.style.color = COLOR_YOU;
  var legendYouText = html("b");
  legendYouText.style.fontWeight = "500";
  legendYou.appendChild(keyYou);
  legendYou.appendChild(legendYouText);
  var legendBench = html("span");
  var keyBench = html("i");
  keyBench.style.color = COLOR_BENCH;
  legendBench.appendChild(keyBench);
  legendBench.appendChild(document.createTextNode(BENCH + "% net retention"));
  legend.appendChild(legendYou);
  legend.appendChild(legendBench);

  /* Chart */
  var svg = el("svg", {
    viewBox: "0 0 " + W + " " + H,
    role: "img",
    "aria-labelledby": "model-chart-title"
  });
  svg.appendChild(el("title", { id: "model-chart-title" },
    "Revenue from an existing customer base over " + YEARS + " years at two net retention rates"));

  var textStyle = { "font-family": "inherit", "font-size": "13", fill: "var(--slate)" };
  for (var g = 0; g <= Y_MAX; g += 10) {
    svg.appendChild(el("line", {
      x1: M.left, x2: M.left + plotW, y1: y(g), y2: y(g),
      stroke: g === 0 ? "var(--chart-axis)" : "var(--chart-grid)", "stroke-width": 1
    }));
    var label = el("text", Object.assign({ x: M.left - 8, y: y(g) + 4, "text-anchor": "end" }, textStyle),
      g === 0 ? "0" : "$" + g + "M");
    svg.appendChild(label);
  }
  for (var t = 0; t <= YEARS; t++) {
    svg.appendChild(el("text", Object.assign({
      x: x(t), y: H - 8, "text-anchor": t === 0 ? "start" : "middle"
    }, textStyle), t === 0 ? "Today" : "Year " + t));
  }

  var cross = el("line", {
    y1: M.top, y2: M.top + plotH, stroke: "var(--chart-cross)", "stroke-width": 1, visibility: "hidden"
  });
  svg.appendChild(cross);

  var lineAttrs = { fill: "none", "stroke-width": 2, "stroke-linejoin": "round", "stroke-linecap": "round" };
  var benchLine = el("path", Object.assign({ d: path(bench), stroke: COLOR_BENCH }, lineAttrs));
  var youLine = el("path", Object.assign({ d: path(you), stroke: COLOR_YOU }, lineAttrs));
  svg.appendChild(benchLine);
  svg.appendChild(youLine);

  function dot(color) {
    return el("circle", { r: 4, fill: color, stroke: "var(--paper)", "stroke-width": 2 });
  }
  var benchEnd = dot(COLOR_BENCH), youEnd = dot(COLOR_YOU);
  var benchHover = dot(COLOR_BENCH), youHover = dot(COLOR_YOU);
  benchHover.setAttribute("visibility", "hidden");
  youHover.setAttribute("visibility", "hidden");
  var endLabel = { x: x(YEARS) + 10, "font-family": "inherit", "font-size": "14", "font-weight": "650", fill: "var(--ink)" };
  var benchValue = el("text", Object.assign({}, endLabel));
  var youValue = el("text", Object.assign({}, endLabel));
  [benchHover, youHover, benchEnd, youEnd, benchValue, youValue].forEach(function (n) { svg.appendChild(n); });

  benchEnd.setAttribute("cx", x(YEARS));
  benchEnd.setAttribute("cy", y(bench[YEARS]));
  benchValue.setAttribute("y", y(bench[YEARS]) + 4.5);
  benchValue.textContent = money(bench[YEARS]);

  var hit = el("rect", { x: M.left, y: M.top, width: plotW, height: plotH, fill: "transparent" });
  svg.appendChild(hit);

  /* Tooltip */
  var tip = html("div", "model__tooltip");
  tip.hidden = true;
  var tipTitle = html("b");
  var tipYou = html("div"), tipBench = html("div");
  var tipYouKey = html("i"), tipBenchKey = html("i");
  tipYouKey.style.color = COLOR_YOU;
  tipBenchKey.style.color = COLOR_BENCH;
  var tipYouValue = html("strong"), tipBenchValue = html("strong");
  var tipYouName = html("span"), tipBenchName = html("span", null, BENCH + "%");
  [tipYouKey, tipYouValue, tipYouName].forEach(function (n) { tipYou.appendChild(n); });
  [tipBenchKey, tipBenchValue, tipBenchName].forEach(function (n) { tipBench.appendChild(n); });
  [tipTitle, tipBench, tipYou].forEach(function (n) { tip.appendChild(n); });

  /* The slider and the numbers table only make sense once this script runs */
  ["model-control", "model-details"].forEach(function (id) {
    var node = document.getElementById(id);
    if (node) node.hidden = false;
  });

  chart.textContent = "";
  chart.appendChild(legend);
  chart.appendChild(svg);
  chart.appendChild(tip);

  /* Readout and table */
  var output = document.getElementById("model-nrr-value");
  var readout = document.getElementById("model-readout");
  var tableHost = document.getElementById("model-table");

  function verb(nrr) {
    if (nrr > 100) return "grows to";
    if (nrr < 100) return "shrinks to";
    return "stays at";
  }

  function render() {
    var nrr = Number(slider.value);
    you = series(nrr);

    youLine.setAttribute("d", path(you));
    youEnd.setAttribute("cx", x(YEARS));
    youEnd.setAttribute("cy", y(you[YEARS]));
    youValue.setAttribute("y", y(you[YEARS]) + 4.5);
    youValue.textContent = money(you[YEARS]);

    if (output) output.textContent = nrr + "%";
    legendYouText.textContent = "Your retention, " + nrr + "%";
    tipYouName.textContent = nrr + "%";
    slider.setAttribute("aria-valuetext", nrr + " percent");

    if (readout) {
      readout.textContent = "";
      readout.appendChild(document.createTextNode(
        "At " + nrr + "% net retention, a " + money(BASE).replace(".0", "") + " customer base " + verb(nrr) + " "));
      readout.appendChild(html("strong", null, money(you[YEARS])));
      readout.appendChild(document.createTextNode(" in " + YEARS + " years. At " + BENCH + "%, it " + verb(BENCH) + " "));
      readout.appendChild(html("strong", null, money(bench[YEARS])));
      readout.appendChild(document.createTextNode("."));
    }

    if (tableHost) {
      var table = html("table");
      var head = html("thead"), headRow = html("tr");
      ["Year", "At " + nrr + "%", "At " + BENCH + "%"].forEach(function (h) {
        var th = html("th", null, h);
        th.scope = "col";
        headRow.appendChild(th);
      });
      head.appendChild(headRow);
      table.appendChild(head);
      var body = html("tbody");
      for (var i = 0; i <= YEARS; i++) {
        var row = html("tr");
        row.appendChild(html("td", null, i === 0 ? "Today" : "Year " + i));
        row.appendChild(html("td", null, money(you[i])));
        row.appendChild(html("td", null, money(bench[i])));
        body.appendChild(row);
      }
      table.appendChild(body);
      tableHost.textContent = "";
      tableHost.appendChild(table);
    }
  }

  /* Hover: the crosshair snaps to the nearest year and one tooltip lists both lines */
  function showAt(clientX) {
    var box = svg.getBoundingClientRect();
    var scale = box.width / W;
    var px = (clientX - box.left) / scale;
    var year = Math.max(0, Math.min(YEARS, Math.round(((px - M.left) / plotW) * YEARS)));
    var cx = x(year);

    cross.setAttribute("x1", cx);
    cross.setAttribute("x2", cx);
    cross.setAttribute("visibility", "visible");
    youHover.setAttribute("cx", cx);
    youHover.setAttribute("cy", y(you[year]));
    benchHover.setAttribute("cx", cx);
    benchHover.setAttribute("cy", y(bench[year]));
    youHover.setAttribute("visibility", "visible");
    benchHover.setAttribute("visibility", "visible");

    tipTitle.textContent = year === 0 ? "Today" : "Year " + year;
    tipYouValue.textContent = money(you[year]);
    tipBenchValue.textContent = money(bench[year]);
    tip.hidden = false;

    var chartBox = chart.getBoundingClientRect();
    var left = box.left - chartBox.left + cx * scale;
    var tipWidth = tip.offsetWidth;
    var placeLeft = year > YEARS / 2 || left + 14 + tipWidth > chartBox.width;
    tip.style.left = Math.max(0, placeLeft ? left - 14 - tipWidth : left + 14) + "px";
    tip.style.top = (box.top - chartBox.top + M.top * scale) + "px";
  }
  function hide() {
    cross.setAttribute("visibility", "hidden");
    youHover.setAttribute("visibility", "hidden");
    benchHover.setAttribute("visibility", "hidden");
    tip.hidden = true;
  }
  hit.addEventListener("pointermove", function (e) { showAt(e.clientX); });
  hit.addEventListener("pointerdown", function (e) { showAt(e.clientX); });
  hit.addEventListener("pointerleave", hide);

  slider.addEventListener("input", render);
  render();

  /* One draw-in on load, skipped for people who ask for reduced motion */
  var still = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (!still && benchLine.getTotalLength) {
    [benchLine, youLine].forEach(function (line, i) {
      var length = line.getTotalLength();
      line.style.strokeDasharray = length;
      line.style.strokeDashoffset = length;
      line.getBoundingClientRect();
      line.style.transition = "stroke-dashoffset 900ms cubic-bezier(.2,.7,.2,1) " + (i ? 0 : 150) + "ms";
      line.style.strokeDashoffset = 0;
      line.addEventListener("transitionend", function () {
        line.style.strokeDasharray = "";
        line.style.strokeDashoffset = "";
        line.style.transition = "";
      }, { once: true });
    });
  }
})();
