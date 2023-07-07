import React from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';

export default function Home(): JSX.Element {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout title={`${siteConfig.title}`} description="Esthesis Platform Documentation">
      <main class="flex-center">
        Esthesis Platform Documentation
      </main>
    </Layout>
  );
}
