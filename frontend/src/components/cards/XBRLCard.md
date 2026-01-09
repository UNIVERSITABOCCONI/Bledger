# XBRLCard Component

## Overview
`XBRLCard.vue` is a reusable UI component for displaying file-related information such as file metadata, title, tags, and status.  
It supports multiple visual states, icons, and action buttons.

---

## Props

| Prop                       | Type                                                                         | Required | Default  | Description                                   |
|----------------------------|------------------------------------------------------------------------------|----------|----------|-----------------------------------------------|
| `metaLabel`                | `string`                                                                     | ✅        | -        | Metadata label displayed above the title.     |
| `title`                    | `string`                                                                     | ✅        | -        | The file name or main title of the card.      |
| `tags`                     | `string[]`                                                                   | ❌        | -        | List of tags displayed under the title.       |
| `status`                   | `'pending' \| 'verified' \| 'download' \| 'request-verification' \| 'basic'` | ✅        | -        | Determines the state and available actions.   |
| `pendingLabel`             | `string`                                                                     | ❌        | -        | Text displayed when the status is `pending`.  |
| `verifiedLabel`            | `string`                                                                     | ❌        | -        | Text displayed when the status is `verified`. |
| `verifiedDate`             | `string`                                                                     | ❌        | -        | Date displayed when the status is `verified`. |
| `downloadLabel`            | `string`                                                                     | ❌        | -        | Button label for download action.             |
| `requestVerificationLabel` | `string`                                                                     | ❌        | -        | Button label for request verification action. |
| `iconType`                 | `'blue' \| 'outline'`                                                        | ❌        | `'blue'` | Defines the icon style.                       |

---

## Slots

| Slot Name | Description |
|-----------|-------------|
| `icon` | Allows overriding the default file icon. |

---

## Events

| Event | Payload | Description |
|-------|---------|-------------|
| `download` | `void` | Triggered when the download button is clicked. |
| `verify` | `void` | Triggered when the request verification button is clicked. |

---

## Status Variants

- **`pending`** → Shows a spinning icon and a pending label.
- **`verified`** → Shows a success icon, verified label, and date.
- **`download`** → Displays a download button.
- **`request-verification`** → Displays a request verification button.
- **`basic`** → Displays only file details without actions.

---

## Example Usage

```vue
<XBRLCard
  metaLabel="Emission details"
  title="report.csv"
  :tags="['Upload']"
  status="download"
  downloadLabel="Download File"
  @download="handleDownload"
/>

<XBRLCard
  metaLabel="Emission details"
  title="report.csv"
  :tags="['Verified']"
  status="verified"
  verifiedLabel="Verified"
  verifiedDate="2025-08-12"
/>
```

---

## Styling
The component uses the `xbrlCard.css` file for styling. The card’s container receives a dynamic CSS class based on its status: `status--pending`, `status--verified`, `status--download`, etc.

---

## Dependencies
- `Button.vue` → Custom button component.
- Icon components: `IconFileOutline.vue`, `IconInProgress.vue`, `IconChecked.vue`, `IconDownload.vue`, `IconFile.vue`, `IconPolygon.vue`.
