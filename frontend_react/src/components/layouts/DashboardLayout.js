import React from 'react';
import Sidebar from './Sidebar';
import Topbar from './Topbar';

const styles = {
  layout: {
    display: 'flex',
    width: '100vw',
    height: '100vh',
  },
  mainContent: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
  },
  pageContainer: {
    flex: 1,
    padding: '30px',
    overflowY: 'auto', // Sayfa içeriği taşarsa scroll çıksın
    backgroundColor: '#f0f2f5',
  },
};

export default function DashboardLayout({ children }) {
  return (
    <div style={styles.layout}>
      <Sidebar />
      <div style={styles.mainContent}>
        <Topbar />
        <main style={styles.pageContainer}>
          {children}
        </main>
      </div>
    </div>
  );
}