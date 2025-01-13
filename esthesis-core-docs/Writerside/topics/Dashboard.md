# Dashboard

[WORK IN PROGRESS]

## Creating a new dashboard component

The implementation of new dashboard components is simple in nature, however there are multiple integration points
that need to be considered. The following steps will guide you through the process of creating a new dashboard
component.

### Frontend

1. Update `app.constants.ts`, creating a new entry for your component under `DASHBOARD.ITEM` constant. The `TYPE`
   property it is the key (i.e. name) to identify your component, the `COLUMNS` is the default number of columns that
   should be assigned to your component, and the `DEFAULTS` is the default configuration for your component. You need to
   define all three properties. If your component should only be included in a dashboard once, add it to the
   `SINGLE_INSTANCE_ITEMS` array.
2. Update `dashboard-item-new.component.ts`, adding your component to the `itemsList` array in `ngOnInit()`.
3. If your component requires custom configuration options, create a DTO to capture your component's 
   configuration under `dto/configuration`. The DTO should be named `dashboard-item-<type>-configuration-dto.ts`. 
4. Create 'view' and 'edit' Angular components for your component under `dashboard/items`. The 'view' component should
   be named `dashboard-item-<type>.component.ts` and the 'edit' component should be named
   `dashboard-item-<type>-edit.component.ts`.
5. The value targeting your component will be available in `lastMessage` variables. If you want your component to also 
   receive real-time notification when that value changes (e.g. to perform a manual action 'on change') you can have 
   your component subscribing to the `lastMessageEmitter` emitter. Check existing components for examples on how 
   to do this.
6. Update `dashboard-view.component.html` to include your component in the `ngxMasonryItem` div.

### Backend

1. Update `AppConstants.java`, creating a new entry for your component under `Dashboard.Type`. The name of this entry
   should match exactly the `TYPE` property defined in the frontend.
2. Create a new class to represent the updates of your component under `esthesis.services.dashboard.impl.dto.update`.
   The class should be named `DashboardUpdate<type>.java`.
3. Update `DashboardUpdateJobFactory.java` to include the job helper for your component. 
4. Update `DashboardUpdateJob.java`, by adding your component in the `execute` method's switch block. Depending on the
   type of your component, you should probably create a helper class to handle the actual update logic.
5. (Optional) Create a configuration class to capture the configuration of your component under
   `esthesis.services.dashboard.impl.dto.config`. The class should be named `DashboardItem<type>Configuration.java`.