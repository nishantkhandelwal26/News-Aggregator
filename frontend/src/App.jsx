import './index.css';
import backgroundImage from './assets/Background Image.png';
import Navbar from './components/Navbar';
import LandingOptions from './components/LandingOptions';
import NewsSources from './components/NewsSources';
import NewsList from './components/NewsList';
import EpapersList from './components/EpapersList';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';

function AppContent() {
  const location = useLocation();
  const isLanding = location.pathname === '/';
  return (
    <div
      className={
        isLanding
          ? 'min-h-screen w-full bg-cover bg-center'
          : 'min-h-screen w-full bg-gray-50'
      }
      style={isLanding ? { backgroundImage: `url(${backgroundImage})` } : {}}
    >
      <Navbar />
      <Routes>
        <Route path="/" element={<LandingOptions />} />
        <Route path="/news" element={<NewsSources />} />
        <Route path="/news/:sourceId" element={<NewsList />} />
        <Route path="/epapers" element={<EpapersList />} />
      </Routes>
    </div>
  );
}

export default function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}
