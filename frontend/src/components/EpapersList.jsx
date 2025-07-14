import { useEffect, useState } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function EpapersList() {
  const [epapers, setEpapers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!API_BASE_URL) return;
    fetch(`${API_BASE_URL}/api/epapers`)
      .then((res) => {
        if (!res.ok) throw new Error('Failed to fetch ePapers');
        return res.json();
      })
      .then((data) => {
        setEpapers(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (!API_BASE_URL) return <div className="flex justify-center items-center min-h-[60vh]"><span className="text-lg text-red-600">API base URL is not set. Please set VITE_API_BASE_URL in your .env file.</span></div>;
  if (loading) return <div className="flex justify-center items-center min-h-[60vh]"><span className="text-lg text-gray-700">Loading ePapers...</span></div>;
  if (error) return <div className="flex justify-center items-center min-h-[60vh]"><span className="text-lg text-red-600">{error}</span></div>;

  return (
    <div className="flex flex-col items-center pt-32 pb-12 px-4">
      <h2 className="text-3xl font-bold text-gray-900 mb-8 text-center drop-shadow">Browse ePapers</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8 w-full max-w-5xl">
        {epapers.map((epaper) => (
          <div key={epaper.id} className="bg-white/80 rounded-lg shadow p-6 flex flex-col items-center gap-4 border border-gray-200 hover:shadow-lg transition-shadow">
            <img src={epaper.logo} alt={epaper.name + ' logo'} className="h-12 w-auto object-contain mb-2" />
            <div className="text-xl font-semibold text-gray-800 text-center">{epaper.name}</div>
            <span className="inline-block px-2 py-0.5 bg-gray-200 text-gray-700 rounded-full text-xs font-semibold mb-1 self-center uppercase tracking-wide">{epaper.category}</span>
            <a
              href={epaper.link}
              target="_blank"
              rel="noopener noreferrer"
              className="mt-2 inline-block px-4 py-2 rounded-md bg-green-600 text-white text-sm font-semibold shadow hover:bg-green-700 transition-colors focus:outline-none focus:ring-2 focus:ring-green-400"
            >
              Open ePaper
            </a>
          </div>
        ))}
      </div>
    </div>
  );
} 