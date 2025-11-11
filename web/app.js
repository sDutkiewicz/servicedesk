import { BASE_API_URL } from "./config.js";

const btn = document.getElementById("pingBtn");
const out = document.getElementById("result");

btn.addEventListener("click", async () => {
  out.textContent = "Łączenie...";
  try {
    const res = await fetch(`${BASE_API_URL}/api/hello`);
    const data = await res.json();
    out.textContent = JSON.stringify(data, null, 2);
  } catch (e) {
    out.textContent = "Błąd: " + e.message;
  }
});
