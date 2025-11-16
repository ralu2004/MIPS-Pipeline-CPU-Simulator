/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
    "./public/index.html",
  ],
  theme: {
    extend: {
      colors: {
        'mips-purple': '#9333ea',
        'mips-pink': '#ec4899',
      },
    },
  },
  plugins: [],
}

