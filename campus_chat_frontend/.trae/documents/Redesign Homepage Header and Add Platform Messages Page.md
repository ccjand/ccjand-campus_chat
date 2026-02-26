I will modify the homepage to match the provided design requirements.

### Implementation Plan

1.  **Modify `src/pages/index/index.vue`**:
    *   **Header Redesign**:
        *   Replace the generic `<top-nav>` component with a custom implementation specific to this page.
        *   Implement a dark blue status bar and title bar with the text "消息".
        *   Add a secondary tab bar below the title with two options: "消息中心" (Message Center) and "平台消息" (Platform Messages).
        *   Implement tab switching logic.
    *   **Content Area Logic**:
        *   Wrap the existing content (Filter Tabs + Message List) in a conditional view shown only when "消息中心" is active.
        *   Add a new section for "平台消息" shown when the second tab is active.
    *   **Platform Messages View**:
        *   Add a search bar at the top of the section.
        *   Implement a list view for platform notifications (Work Notification, To-do, Announcements, etc.).
        *   Use mock data to populate the platform messages list, matching the icons and text from the second image.

### Technical Details
*   **Colors**: Dark Blue header (`#3C4A80` or similar based on image), White content background.
*   **Components**: Use `uview-plus` components (like `u-icon`, `u-badge`) where applicable, consistent with the existing project.
*   **State**: Add `currentMainTab` ref to track the top-level tab selection.
