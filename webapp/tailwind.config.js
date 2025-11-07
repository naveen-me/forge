/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#007AFF",
        "background-light": "#F9FAFB",
        "background-dark": "#111827",
        "card-light": "#FFFFFF",
        "card-dark": "#1F2937",
        "text-light": "#1F2937",
        "text-dark": "#F9FAFB",
        "subtext-light": "#6B7280",
        "subtext-dark": "#9CA3AF",
        "border-light": "#E5E7EB",
        "border-dark": "#374151",
      },
      fontFamily: {
        sans: ["Inter", "sans-serif"],
      },
      borderRadius: {
        DEFAULT: "0.5rem",
      },
      boxShadow: {
        'subtle': '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05)',
        'around': '0 0 15px rgba(0, 0, 0, 0.1)',
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}