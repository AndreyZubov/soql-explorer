import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from './features/auth/LoginPage';
import RequireAuth from './features/auth/RequireAuth';
import AppLayout from './components/layout/AppLayout';
import ConnectionsPage from './features/connections/ConnectionsPage';
import ExplorerPage from './features/explorer/ExplorerPage';
import HistoryPage from './features/history/HistoryPage';
import TemplatesPage from './features/templates/TemplatesPage';

// Top-level router. Routes for features that arrive in later steps (explorer,
// history, templates, connections) are wired now and render placeholder
// pages so the navigation skeleton is testable end-to-end.
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <AppLayout />
          </RequireAuth>
        }
      >
        <Route path="/" element={<Navigate to="/connections" replace />} />
        <Route path="/connections" element={<ConnectionsPage />} />
        <Route path="/explorer/:connectionId" element={<ExplorerPage />} />
        <Route path="/history" element={<HistoryPage />} />
        <Route path="/templates" element={<TemplatesPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
