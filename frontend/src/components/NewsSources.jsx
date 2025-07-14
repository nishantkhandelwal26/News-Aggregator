import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function NewsSources() {
  const [sources, setSources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!API_BASE_URL) return;
    fetch(`${API_BASE_URL}/api/news-sources`)
      .then((res) => {
        if (!res.ok) throw new Error('Failed to fetch news sources');
        return res.json();
      })
      .then((data) => {
        setSources(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (!API_BASE_URL) return <div className="flex justify-center items-center min-h-[60vh]"><span className="text-lg text-red-600">API base URL is not set. Please set VITE_API_BASE_URL in your .env file.</span></div>;
  if (loading) return <div className="flex justify-center items-center min-h-[60vh]"><span className="text-lg text-gray-700">Loading news sources...</span></div>;
  if (error) return <div className="flex justify-center items-center min-h-[60vh]"><span className="text-lg text-red-600">{error}</span></div>;

  return (
    <div className="flex flex-col items-center pt-32 pb-12 px-4">
      <h2 className="text-3xl font-bold text-gray-900 mb-8 text-center drop-shadow">News Sources</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8 w-full max-w-5xl">
        {sources.map((src, idx) => (
          <button
            key={idx}
            className="bg-white/80 rounded-lg shadow p-6 flex flex-col items-center gap-4 border border-gray-200 hover:shadow-lg transition-shadow cursor-pointer focus:outline-none focus:ring-2 focus:ring-blue-400"
            onClick={() => navigate(`/news/${src.id}`, { state: { name: src.name, logo: src.logo } })}
          >
            <div className="flex flex-col items-center gap-2 w-full">
              <img src={src.logo} alt={src.name + ' logo'} className="h-12 w-auto object-contain mb-2" />
              <div className="text-xl font-semibold text-gray-800 text-center">{src.name}</div>
            </div>
            <span className="inline-block px-2 py-0.5 bg-gray-200 text-gray-700 rounded-full text-xs font-semibold mb-1 self-center uppercase tracking-wide">{src.category}</span>
            <span className="mt-2 inline-block px-4 py-2 rounded-md bg-blue-600 text-white text-sm font-semibold shadow hover:bg-blue-700 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-400">View News</span>
          </button>
        ))}
      </div>
    </div>
  );
} 