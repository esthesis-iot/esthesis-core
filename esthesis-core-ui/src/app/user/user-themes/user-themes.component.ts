import {Component} from "@angular/core";

@Component({
  selector: 'app-user-themes',
  templateUrl: './user-themes.component.html'
})
export class UserThemesComponent {
  daisyui = {
    themes: [
      "acid",
      "aqua",
      "autumn",
      "black",
      "bumblebee",
      "business",
      "cmyk",
      "coffee",
      "corporate",
      "cupcake",
      "cyberpunk",
      "dark",
      "dim",
      "dracula",
      "emerald",
      "esthesis",
      "fantasy",
      "forest",
      "garden",
      "halloween",
      "lemonade",
      "light",
      "lofi",
      "luxury",
      "night",
      "nord",
      "pastel",
      "retro",
      "sunset",
      "synthwave",
      "valentine",
      "winter",
      "wireframe"
    ]}

  selectTheme(name: string) {
    // Change the theme.
    document.querySelector("html")!.setAttribute("data-theme", name);

    // Save the selection in local storage.
    localStorage.setItem("theme", name);
  }
}
