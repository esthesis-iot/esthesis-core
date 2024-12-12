import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
  selector: "app-topbar",
  templateUrl: "./topbar.component.html"
})
export class TopbarComponent implements OnInit {
  form!: FormGroup;
  public isMenuOpen = false;
  themes = ["acid", "aqua", "autumn", "black", "bumblebee", "business", "cmyk", "coffee", "corporate",
    "cupcake", "cyberpunk", "dark", "dracula", "emerald", "fantasy", "forest", "garden", "halloween",
    "lemonade", "light", "lofi", "luxury", "night", "pastel", "retro", "synthwave", "valentine",
    "winter", "wireframe"];

  constructor(private fb: FormBuilder) {
  }

  ngOnInit(): void {
    // Set up the form.
    this.form = this.fb.group({
      searchTerm: []
    });
  }

  public toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  closeSearchResults() {
    this.form.controls["searchTerm"].setValue("");
  }

  selectTheme(name: string) {
    // Change the theme.
    document.querySelector("html")!.setAttribute("data-theme", name);

    // Save the selection in local storage.
    localStorage.setItem("theme", name);
  }
}
