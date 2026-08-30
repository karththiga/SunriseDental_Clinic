(function () {
    "use strict";

    window.clinicFocusSection = function (id) {
        var target = document.getElementById(id);
        if (!target) return;
        var reducedMotion = window.matchMedia &&
                window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        target.focus({preventScroll: true});
        target.scrollIntoView({
            behavior: reducedMotion ? "auto" : "smooth",
            block: "start"
        });
    };

    // Horizontally scrollable data regions must also be reachable by keyboard.
    document.querySelectorAll(".table-container, .table-wrap").forEach(function (tableRegion) {
        if (!tableRegion.hasAttribute("tabindex")) tableRegion.tabIndex = 0;
        if (!tableRegion.hasAttribute("role")) tableRegion.setAttribute("role", "region");
        if (!tableRegion.hasAttribute("aria-label")) {
            tableRegion.setAttribute("aria-label", "Scrollable data table");
        }
    });
}());
