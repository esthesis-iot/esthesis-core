// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
	title: 'esthesis CORE',
	tagline: 'Documentation',
	favicon: 'img/logo.png',

	// Set the production url of your site here
	url: 'https://esthes.is',
	// Set the /<baseUrl>/ pathname under which your site is served
	// For GitHub pages deployment, it is often '/<projectName>/'
	baseUrl: '/',

	markdown: {
		mermaid: true
	},
	themes: ['@docusaurus/theme-mermaid'],

	// GitHub pages deployment config.
	// If you aren't using GitHub pages, you don't need these.
	organizationName: 'esthesis-iot', // Usually your GitHub org/user name.
	projectName: 'esthesis-platform', // Usually your repo name.

	onBrokenLinks: 'throw',
	onBrokenMarkdownLinks: 'warn',

	// Even if you don't use internalization, you can use this field to set useful
	// metadata like html lang. For example, if your site is Chinese, you may want
	// to replace "en" with "zh-Hans".
	i18n: {
		defaultLocale: 'en',
		locales: ['en'],
	},

	presets: [
		[
			'classic',
			/** @type {import('@docusaurus/preset-classic').Options} */
			({
				docs: {
					routeBasePath: '/',
					sidebarPath: require.resolve('./sidebars.js'),
					// editUrl:
					//   'https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/',
				},
				theme: {
					customCss: require.resolve('./src/css/custom.css'),
				},
			}),
		],
	],

	themeConfig:
	/** @type {import('@docusaurus/preset-classic').ThemeConfig} */
		(
			{
				navbar: {
					title: 'esthesis Core',
					logo: {
						alt: 'esthesis',
						src: 'img/logo.png',
					},
					items: [
						{
							type: 'docsVersionDropdown',
							position: 'right',
							dropdownActiveClassDisabled: true,
						},
						{
							type: 'doc',
							docId: 'index',
							position: 'left',
							label: 'Documentation',
						},
						{
							type: 'doc',
							docId: 'installation-guide/index',
							position: 'left',
							label: 'Installation',
						},
						{
							type: 'doc',
							docId: 'developers-guide/index',
							position: 'left',
							label: 'Developers',
						},
						    {
						      type: 'doc',
						      docId: 'contact-support/index',
						      position: 'left',
						      label: 'Contact',
						    },
						    {
						      href: 'https://esthes.is',
						      position: 'right',
						      label: 'Edge',
						    },
						    {
						      href: 'https://esthes.is',
						      position: 'right',
						      label: 'Gateway',
						    },
						    {
						      href: 'https://esthes.is',
						      position: 'right',
						      label: 'UNS',
						    },						    {
						      href: 'https://esthes.is',
						      position: 'right',
						      label: 'Historian',
						    },
						    {
						      href: 'https://esthes.is',
						      label: 'Website',
						      position: 'right',
						    },
						    {
						      href: 'https://github.com/esthesis-iot',
						      label: 'GitHub',
						      position: 'right',
						    },
					],
				},
				footer: {
					style: 'dark',
					links: [
						{
							title: 'Products',
							items: [
								{label: 'esthesis Core', to: 'https://esthes.is'},
								{label: 'esthesis Edge', to: 'https://esthes.is'},
								{label: 'esthesis Gateway', to: 'https://esthes.is'},
								{label: 'esthesis UNS', to: 'https://esthes.is'},
								{label: 'esthesis Historian', to: 'https://esthes.is'},
							],
						},
						{
							title: 'Documentation',
							items: [
								{label: 'esthesis Core', to: 'https://esthes.is'},
								{label: 'esthesis Edge', to: 'https://esthes.is'},
								{label: 'esthesis Gateway', to: 'https://esthes.is'},
								{label: 'esthesis UNS', to: 'https://esthes.is'},
								{label: 'esthesis Historian', to: 'https://esthes.is'},
							],
						},
						{
							title: 'Tutorials',
							items: [
								{label: 'esthesis YouTube', to: 'https://esthes.is'},
							],
						},
						{
							title: 'Social',
							items: [
								{
									label: 'Twitter',
									to: 'https://twitter.com',
								},
								{
									label: 'YouTube',
									to: 'https://youtube.com',
								},
							],
						},
						{
							title: 'Contact',
							items: [
								{label: 'Contact page', to: 'https://www.eurodyn.com/contact'},
							],
						},
					],
					copyright: `<div><a href="https://www.eurodyn.com" target="_blank">European Dynamics</a> &copy; ${new Date().getFullYear()}</div>`,
				},
				prism: {
					theme: lightCodeTheme,
					darkTheme: darkCodeTheme,
				}
			}),
};

module.exports = config;
