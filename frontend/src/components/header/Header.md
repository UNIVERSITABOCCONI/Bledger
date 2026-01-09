# Header Component

## Overview
The `Header` component is a reusable, accessible application header that includes:
- A logo section (emits a `logo-click` event when clicked).
- A navigation menu with active state management.
- A logout button (emits a `logout` event when clicked).

It is designed to be **presentational only**, delegating navigation and business logic to the parent component.

---

## Props

| Name         | Type       | Default   | Description |
|--------------|-----------|-----------|-------------|
| `navItems`   | `NavItem[]` | **Required** | An array of navigation items to display in the center menu. |
| `logoutLabel`| `string`   | `'Logout'` | The text label for the logout button. |

**NavItem Interface:**
```ts
interface NavItem {
  label: string;
  active: boolean;
  to: string;
}
```

---

## Events

| Event Name     | Payload              | Description |
|----------------|----------------------|-------------|
| `logo-click`   | `MouseEvent`          | Fired when the logo is clicked. |
| `logout`       | `MouseEvent`          | Fired when the logout button is clicked. |

---

## Slots

| Slot Name   | Description                                         | Default Content  |
|-------------|-----------------------------------------------------|------------------|
| `logo`      | Custom logo content                                 | Default SVG logo |
| *(default)* | Navigation menu items are generated from `navItems` | -                |

---

## Accessibility
- The navigation menu is wrapped in a `<nav>` with `aria-label="Main"`.
- The logo area is clickable and focusable via mouse or keyboard.
- The logout button is an accessible `<button>` element.

---

## Example Usage
```vue
<script setup lang="ts">
import Header from "@/components/header/Header.vue";
import { useRouter } from "vue-router";

const router = useRouter();
const nav = [
  { label: "Home", active: true, to: "/" },
  { label: "About", active: false, to: "/about" },
];

function handleLogout() {
  console.log("User logged out");
}
</script>

<template>
  <Header
    :navItems="nav"
    logoutLabel="Sign out"
    @logo-click="() => router.push('/')"
    @logout="handleLogout"
  >
    <template #logo>
      <img src="/my-logo.png" alt="App Logo" />
    </template>
  </Header>
</template>
```

---

## Styling
The component imports `header.css` for layout and design.
You can override styles in your global or scoped CSS.

