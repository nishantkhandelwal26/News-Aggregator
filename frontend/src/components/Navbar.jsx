import { useNavigate, Link } from 'react-router-dom';

export default function Navbar() {
  const navigate = useNavigate();
  return (
    <nav className="w-full flex items-center justify-between px-8 py-4 bg-white bg-opacity-80 shadow-md fixed top-0 left-0 z-50 backdrop-blur-md">
      <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate('/') }>
        <span className="text-2xl font-extrabold text-blue-700 tracking-tight">News Portal</span>
      </div>
      <div className="flex gap-6">
        <Link to="/news" className="text-gray-700 hover:text-blue-600 font-medium transition-colors">Explore News</Link>
        <Link to="/epapers" className="text-gray-700 hover:text-green-600 font-medium transition-colors">Browse ePapers</Link>
      </div>
    </nav>
  );
} 