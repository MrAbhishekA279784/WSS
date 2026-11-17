import { defineConfig } from "eslint/config";
import next from "eslint-config-next";

export default defineConfig([
  {
    ignores: [".next-dev/**", ".next/**", "target/**", ".playwright-cli/**"],
  },
  {
    extends: [...next],
  },
]);
