(function () {
    "use strict";
    var weekdayNames = ["SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"];
    function iso(date) {
        return date.getFullYear() + "-" + String(date.getMonth() + 1).padStart(2, "0")
                + "-" + String(date.getDate()).padStart(2, "0");
    }
    window.initClinicBookingCalendar = function (config) {
        var dentist = document.getElementById(config.dentistSelectId);
        var calendar = document.getElementById(config.calendarId);
        if (!dentist || !calendar) return;
        function render() {
            calendar.innerHTML = "";
            var option = dentist.options[dentist.selectedIndex];
            var days = option && option.dataset.days ? option.dataset.days.split(",") : [];
            var leaves = new Set(option && option.dataset.leaves ? option.dataset.leaves.split(",") : []);
            if (!option || !option.value) {
                calendar.innerHTML = '<p class="calendar-empty">Select a dentist to view eligible dates.</p>';
                return;
            }
            var dates = [];
            var cursor = new Date(); cursor.setHours(12, 0, 0, 0);
            for (var offset = 0; offset < 90; offset++) {
                var date = new Date(cursor); date.setDate(cursor.getDate() + offset);
                var value = iso(date);
                if (days.indexOf(weekdayNames[date.getDay()]) >= 0 && !leaves.has(value)) dates.push(date);
            }
            if (!dates.length) {
                calendar.innerHTML = '<p class="calendar-empty">No bookable dates are available in the next 90 days.</p>';
                return;
            }
            dates.forEach(function (date) {
                var value = iso(date), label = document.createElement("label");
                label.className = "date-option";
                var input = document.createElement("input");
                input.type = "radio"; input.name = "appointmentDate"; input.value = value; input.required = true;
                input.checked = value === config.selectedDate;
                var text = document.createElement("span");
                text.innerHTML = "<strong>" + date.toLocaleDateString(undefined, {weekday:"short"}) + "</strong>"
                        + date.toLocaleDateString(undefined, {day:"numeric",month:"short"});
                label.appendChild(input); label.appendChild(text); calendar.appendChild(label);
            });
        }
        dentist.addEventListener("change", function () { render(); if (window.clinicFocusSection) clinicFocusSection(config.calendarId); });
        render();
    };
}());
