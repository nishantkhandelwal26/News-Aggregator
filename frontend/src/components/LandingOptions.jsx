import { useNavigate } from 'react-router-dom';

export default function LandingOptions() {
  const navigate = useNavigate();
  return (
    <div className="flex flex-col items-center justify-center min-h-screen w-full pt-32 gap-8">
      <div className="backdrop-blur-sm bg-black/30 rounded-lg px-6 py-8 flex flex-col items-center gap-6 max-w-2xl w-full">
        <h1 className="text-5xl sm:text-6xl font-extrabold text-white text-center drop-shadow-lg tracking-tight leading-tight">
          Welcome to <span className="text-blue-300">News Aggregator</span>
        </h1>
        <p className="text-lg sm:text-2xl font-medium text-white text-center drop-shadow max-w-xl">
          Stay informed and explore the world of news and ePapers with a seamless experience.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 mt-2">
          <button
            onClick={() => navigate('/news')}
            className="py-2 px-6 rounded-md border border-blue-300 text-blue-100 bg-white/10 hover:bg-blue-400/20 transition-colors text-lg font-semibold text-center shadow-sm backdrop-blur"
          >
            Explore News
          </button>
          <button
            onClick={() => navigate('/epapers')}
            className="py-2 px-6 rounded-md border border-green-300 text-green-100 bg-white/10 hover:bg-green-400/20 transition-colors text-lg font-semibold text-center shadow-sm backdrop-blur"
          >
            Browse ePapers
          </button>
        </div>
      </div>
    </div>
  );
} 