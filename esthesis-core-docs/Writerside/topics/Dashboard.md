# Dashboard

[WORK IN PROGRESS]

## Creating a new dashboard component

The implementation of new dashboard components is simple in nature, however there are multiple integration points
that need to be considered. The following steps will guide you through the process of creating a new dashboard
component:

### Frontend

1. Update `app.constants.ts`, creating a new entry for your component under `DASHBOARD.ITEM` constant. the `TYPE`
   property it is the key to identify the component, the `COLUMNS` is the default number of columns that should be
   assigned to your component, and the `DEFAULTS` is the default configuration for your component. You need to define
   all three properties. If your component should only be included in a dashboard once, add it to the
   `SINGLE_INSTANCE_ITEMS` array.
2. Update `dashboard-item-new.component.ts`, adding your component to the `itemsList` array.
3. Create 'view' and 'edit' Angular components for your component under `dashboard/items`. The 'view' component should
   be named `dashboard-item-<type>.component.ts` and the 'edit' component should be named
   `dashboard-item-<type>-edit.component.ts`.
4. To receive dashboard updates you need to have your component subscribing to the `dashboardEmitter` emitter of
   `DashboardService`.

### Backend

1. Update `AppConstants.java`, creating a new entry for your component under `Dashboard.Type`. The name of this entry
   should match exactly the `TYPE` property defined in the frontend.
2. Create a new class to represent the updates of your component under `esthesis.services.dashboard.impl.dto.update`.
   The class should be named `DashboardUpdate<type>.java`.
3. Update `DashboardUpdate.java` class, to include an entry for your component (this is the object that accumulates all
   updates for a given dashboard and then forwarded to the frontend via SSE).
4. Update `DashboardUpdateJob.java`, by adding your component in the `execute` method's switch block. Depending on the
   type of your component, you should probably create a private method to handle the actual update logic.
5. (Optional) Create a configuration class to capture the configuration of your component under
   `esthesis.services.dashboard.impl.dto.config`. The class should be named `DashboardItem<type>Configuration.java`.