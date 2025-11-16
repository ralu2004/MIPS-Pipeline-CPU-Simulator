# MIPS Pipeline Simulator - Frontend

A modern React + Tailwind CSS frontend for the MIPS Pipeline Simulator.

## Features

- 🎨 Beautiful, modern UI with Tailwind CSS
- 🔄 Real-time pipeline visualization
- 💾 Register and memory display
- ⚡ Step-by-step and auto-execution modes
- 📚 Sample programs included
- ✏️ Custom program loading
- 📊 Detailed pipeline stage information

## Prerequisites

- Node.js (v14 or higher)
- npm or yarn
- Backend API server running on port 8080 (default)

## Installation

1. Navigate to the frontend directory:
```bash
cd Simulator-Frontend/simulator
```

2. Install dependencies:
```bash
npm install
```

## Configuration

The frontend connects to the backend API at `http://localhost:8080` by default.

To change the API URL, create a `.env` file in the `simulator` directory:
```
REACT_APP_API_BASE=http://localhost:8080
```

## Running the Application

1. Make sure the backend API server is running (see backend README)

2. Start the React development server:
```bash
npm start
```

3. Open [http://localhost:3000](http://localhost:3000) in your browser

## Building for Production

To create a production build:

```bash
npm run build
```

The build folder will contain the optimized production build.

## Project Structure

```
simulator/
├── public/          # Static files
├── src/
│   ├── App.js       # Main application component
│   ├── index.js     # React entry point
│   └── index.css    # Global styles with Tailwind
├── package.json     # Dependencies and scripts
└── tailwind.config.js  # Tailwind CSS configuration
```

## Usage

1. **Load a Program**: Select a sample program or enter custom hex instructions
2. **Step Execution**: Use "Step 1 Cycle" or "Step 5 Cycles" to advance execution
3. **Auto Run**: Click "Run Auto" to continuously execute cycles
4. **View State**: The pipeline, registers, and memory update automatically
5. **Reset**: Click "Reset All" to clear the simulator state

## API Endpoints Used

- `POST /api/load?start=0` - Load program into instruction memory
- `POST /api/step?cycles=N` - Execute N clock cycles
- `GET /api/state` - Get current CPU and pipeline state
- `POST /api/reset?clearRegs=1&clearMem=1&pc=0` - Reset simulator

## Technologies

- **React 19** - UI framework
- **Tailwind CSS 3** - Styling
- **Lucide React** - Icons
- **Fetch API** - HTTP requests

## Troubleshooting

### Cannot connect to server
- Ensure the backend API server is running on port 8080
- Check that CORS is enabled on the backend
- Verify the API_BASE URL in `.env` matches your backend

### Build errors
- Delete `node_modules` and `package-lock.json`, then run `npm install` again
- Ensure Node.js version is 14 or higher

### Styling issues
- Make sure Tailwind CSS is properly configured
- Check that `tailwind.config.js` includes the correct content paths
