
# Image Component

## Description
The **Image** component is a simple, reusable wrapper for displaying images in a `<picture>` element, with optional alt text and a fallback placeholder.  
It is intended to improve semantic HTML structure and provide consistent styling for images across the application.

---

## Props

| Prop  | Type   | Default                  | Required  | Description                                                          |
|-------|--------|--------------------------|-----------|----------------------------------------------------------------------|
| `src` | string | `/image-placeholder.png` | No        | The image URL to display. Defaults to a placeholder if not provided. |
| `alt` | string | `"Image"`                | No        | Alternative text for accessibility and SEO.                          |

---

## Usage Example

```vue
<Image src="/assets/company-logo.png" alt="Company Logo" />
```

If no `src` is provided:

```vue
<Image />
```
This will display the placeholder image.

---

## Accessibility
- The `alt` prop is used for screen readers and should describe the image content.
- Defaults to `"Image"` if not provided.

---

## Styling
The component uses scoped CSS from `@/styles/image.css` to ensure consistent presentation.  
You can override these styles by targeting the `.image-picture` or `.image-picture-image` classes.

---

## Slots
This component **does not** have slots.

---

## Best Practices
- Always provide a meaningful `alt` text for accessibility.
- Use appropriately sized images to improve performance.
- Consider using different image sources inside `<picture>` for responsive image handling.
