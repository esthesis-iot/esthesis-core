/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'midnight': {
          '50': '#f2f3fb',
          '100': '#e7e9f8',
          '200': '#d4d7f1',
          '300': '#babde7',
          '400': '#9d9ddc',
          '500': '#8b85cf',
          '600': '#776cbf',
          '700': '#675ba7',
          '800': '#312d4b',
          '900': '#27243c',
        }
      },
      fontFamily: {
        "inter": ["inter", "sans-serif", "Helvetica Neue", "arial",
          "sans-serif"]
      }
    },
  },
  plugins: [
    // require('@tailwindcss/forms'),
    require("daisyui")
  ],
  darkMode: "class"
}
