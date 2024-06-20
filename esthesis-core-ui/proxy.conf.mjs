import { EventEmitter } from "events";
EventEmitter.defaultMaxListeners = Infinity;

export default [
  {
    context: ["/api/about"],
    target: "http://127.0.0.1:59120",
    pathRewrite: { "/about": "/" },
    secure: false,
  },
  {
    context: ["/api/agent"],
    target: "http://127.0.0.1:59070",
    pathRewrite: { "/agent": "/" },
    secure: false,
  },
  {
    context: ["/api/application"],
    target: "http://127.0.0.1:59090",
    pathRewrite: { "/application": "/" },
    secure: false,
  },
  {
    context: ["/api/audit"],
    target: "http://127.0.0.1:59140",
    pathRewrite: { "/audit": "/" },
    secure: false,
  },
  {
    context: ["/api/campaign"],
    target: "http://127.0.0.1:59150",
    pathRewrite: { "/campaign": "/" },
    secure: false,
  },
  {
    context: ["/api/command"],
    target: "http://127.0.0.1:59080",
    pathRewrite: { "/command": "/" },
    secure: false,
  },
  {
    context: ["/api/crypto"],
    target: "http://127.0.0.1:59040",
    pathRewrite: { "/crypto": "/" },
    secure: false,
  },
  {
    context: ["/api/dataflow"],
    target: "http://127.0.0.1:59060",
    pathRewrite: { "/dataflow": "/" },
    secure: false,
  },
  {
    context: ["/api/device"],
    target: "http://127.0.0.1:59010",
    pathRewrite: { "/device": "/" },
    secure: false,
  },
  {
    context: ["/api/dt"],
    target: "http://127.0.0.1:59130",
    secure: false,
  },
  {
    context: ["/api/infrastructure"],
    target: "http://127.0.0.1:59110",
    pathRewrite: { "/infrastructure": "/" },
    secure: false,
  },
  {
    context: ["/api/kubernetes"],
    target: "http://127.0.0.1:59050",
    pathRewrite: { "/kubernetes": "/" },
    secure: false,
  },
  {
    context: ["/api/provisioning"],
    target: "http://127.0.0.1:59100",
    pathRewrite: { "/provisioning": "/" },
    secure: false,
  },
  {
    context: ["/api/public-access"],
    target: "http://127.0.0.1:59160",
    pathRewrite: { "/public-access": "/" },
    secure: false,
  },
  {
    context: ["/api/security"],
    target: "http://127.0.0.1:59170",
    pathRewrite: { "/security": "/" },
    secure: false,
  },
  {
    context: ["/api/settings"],
    target: "http://127.0.0.1:59030",
    pathRewrite: { "/settings": "/" },
    secure: false,
  },
  {
    context: ["/api/tag"],
    target: "http://127.0.0.1:59020",
    pathRewrite: { "/tag": "/" },
    secure: false,
  },
];
