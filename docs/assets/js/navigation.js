"use strict";

(() => {
  const navigation = document.querySelector(".site-nav");
  const navigationToggle = navigation && navigation.querySelector(".nav-toggle");

  if (navigationToggle) {
    navigationToggle.addEventListener("click", () => {
      const expanded = navigationToggle.getAttribute("aria-expanded") === "true";
      navigationToggle.setAttribute("aria-expanded", String(!expanded));
    });

    document.addEventListener("keydown", event => {
      if (event.key === "Escape"
          && !event.defaultPrevented
          && navigationToggle.getAttribute("aria-expanded") === "true"
          && navigationToggle.getClientRects().length > 0) {
        navigationToggle.setAttribute("aria-expanded", "false");
        navigationToggle.focus();
        event.preventDefault();
      }
    });

    navigationToggle.hidden = false;
    navigation.classList.add("nav-ready");
  }
})();
