import {browser} from "k6/browser";
import {Auth} from "./tests/auth.js";
import {Devices} from "./tests/devices.js";
import {Command} from "./tests/command.js";
import {Provisioning} from "./tests/provisioning.js";
import {Campaigns} from "./tests/campaigns.js";
import {CAs} from "./tests/cas.js";
import {Keystores} from "./tests/keystores.js";
import {DataFlows} from "./tests/dataflows.js";
import {Infrastructure} from "./tests/infrastructure.js";
import {Applications} from "./tests/applications.js";
import {Tags} from "./tests/tags.js";
import {Settings} from "./tests/settings.js";
import {Users} from "./tests/users.js";
import {Groups} from "./tests/groups.js";
import {Roles} from "./tests/roles.js";
import {Policies} from "./tests/policies.js";
import {Audit} from "./tests/audit.js";
import {About} from "./tests/about.js";
import {Certificates} from "./tests/certificates.js";

// Execution options.
export const options = {
  scenarios: {
    ui: {
      executor: "shared-iterations",
      options: {
        browser: {
          type: "chromium",
        },
      },
    },
  },
};

export default async function () {
  const page = await browser.newPage();
  try {
    await new Auth(page).test();
    await new Devices(page).test();
    await new Command(page).test();
    await new Provisioning(page).test();
    await new Campaigns(page).test();
    await new Certificates(page).test();
    await new CAs(page).test();
    await new Keystores(page).test();
    await new DataFlows(page).test();
    await new Infrastructure(page).test();
    await new Applications(page).test();
    await new Tags(page).test();
    await new Settings(page).test();
    await new Users(page).test();
    await new Groups(page).test();
    await new Roles(page).test();
    await new Policies(page).test();
    await new Audit(page).test();
    await new About(page).test();
  } finally {
    await page.close();
  }
}
