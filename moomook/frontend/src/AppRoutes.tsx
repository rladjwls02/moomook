import { Route, Routes } from "react-router-dom";
import App from "./App";
import AdminMenuPage from "./pages/AdminMenuPage";

const AppRoutes = () => (
  <Routes>
    <Route path="/" element={<App />} />
    <Route path="/admin" element={<AdminMenuPage />} />
  </Routes>
);

export default AppRoutes;
