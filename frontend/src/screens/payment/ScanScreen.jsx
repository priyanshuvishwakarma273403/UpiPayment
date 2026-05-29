import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Camera, Image, Zap, ZapOff, ArrowLeft, Keyboard, Sparkles, Check } from 'lucide-react';
import { Html5Qrcode } from 'html5-qrcode';
import toast from 'react-hot-toast';

import { useHaptic } from '../../hooks/useHaptic';
import TopBar from '../../components/layout/TopBar';
import BottomSheet from '../../components/ui/BottomSheet';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import { validateUpi } from '../../utils/validateUpi';

export const ScanScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();

  const [flashlight, setFlashlight] = useState(false);
  const [cameraActive, setCameraActive] = useState(false);
  const [isManualSheetOpen, setIsManualSheetOpen] = useState(false);
  const [manualUpi, setManualUpi] = useState('');
  
  const qrScannerRef = useRef(null);
  const scannerContainerId = 'reader';

  // Initialize html5-qrcode scanner
  useEffect(() => {
    let html5Qrcode = null;

    const startScanner = async () => {
      try {
        html5Qrcode = new Html5Qrcode(scannerContainerId);
        qrScannerRef.current = html5Qrcode;

        const config = { fps: 10, qrbox: { width: 250, height: 250 } };
        
        await html5Qrcode.start(
          { facingMode: 'environment' },
          config,
          (decodedText) => {
            handleScanSuccess(decodedText);
          },
          (errorMessage) => {
            // silent fail on scan updates
          }
        );
        setCameraActive(true);
      } catch (err) {
        console.warn('Camera failed to start. Running simulated scanner fallback:', err);
        setCameraActive(false);
      }
    };

    startScanner();

    // Cleanup scanner on unmount
    return () => {
      if (qrScannerRef.current && qrScannerRef.current.isScanning) {
        qrScannerRef.current.stop()
          .then(() => console.log('Scanner stopped.'))
          .catch((e) => console.error(e));
      }
    };
  }, []);

  const handleScanSuccess = (decodedText) => {
    triggerHaptic('success');
    
    // Stop scanner
    if (qrScannerRef.current && qrScannerRef.current.isScanning) {
      qrScannerRef.current.stop().catch(e => console.error(e));
    }

    toast.success('QR Code scanned successfully!');

    // Check if the decoded text looks like a UPI ID
    if (validateUpi(decodedText)) {
      navigate(`/send?upi=${decodedText}`);
    } else if (decodedText.startsWith('upi://pay')) {
      // Parse standard UPI deep links: upi://pay?pa=recipient@upi&pn=Name...
      try {
        const urlParams = new URLSearchParams(decodedText.split('?')[1]);
        const pa = urlParams.get('pa'); // UPI ID
        if (pa) {
          navigate(`/send?upi=${pa}`);
        } else {
          toast.error('Invalid payment QR parameters');
        }
      } catch (e) {
        navigate(`/send?upi=demo@upimesh`);
      }
    } else {
      // General routing fallback
      navigate(`/send?upi=${decodedText}`);
    }
  };

  const handleMockScan = (demoUpi = 'aarav@upimesh') => {
    triggerHaptic('success');
    toast.success('Simulation: QR Code parsed successfully');
    navigate(`/send?upi=${demoUpi}`);
  };

  const handleManualProceed = () => {
    if (!validateUpi(manualUpi)) {
      toast.error('Please enter a valid UPI ID (e.g. name@upimesh)');
      return;
    }
    triggerHaptic('light');
    setIsManualSheetOpen(false);
    navigate(`/send?upi=${manualUpi}`);
  };

  const toggleFlash = () => {
    triggerHaptic('light');
    setFlashlight(prev => !prev);
    // Simple state toggle in sandbox demo
    toast(flashlight ? 'Flashlight Off' : 'Flashlight On', { icon: '🔦' });
  };

  const handleGalleryUpload = (e) => {
    triggerHaptic('light');
    // Simulate image uploading parses
    toast.loading('Processing image file...');
    setTimeout(() => {
      toast.dismiss();
      handleMockScan('ishita@upimesh');
    }, 1200);
  };

  return (
    <div className="flex-1 flex flex-col bg-black text-white h-full relative select-none">
      {/* Absolute top action header */}
      <div className="absolute top-10 left-0 right-0 h-14 px-4 flex items-center justify-between z-50">
        <button
          onClick={() => { triggerHaptic('light'); navigate(-1); }}
          className="p-2 rounded-full bg-black/40 backdrop-blur-md border border-white/10 text-white"
          aria-label="Back"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <span className="text-sm font-bold tracking-wide">Scan QR Code</span>
        <button
          onClick={toggleFlash}
          className="p-2 rounded-full bg-black/40 backdrop-blur-md border border-white/10 text-white"
        >
          {flashlight ? <Zap className="w-5 h-5 text-warning fill-warning" /> : <ZapOff className="w-5 h-5" />}
        </button>
      </div>

      {/* Main Viewfinder Section */}
      <div className="flex-1 flex flex-col items-center justify-center relative bg-slate-950 overflow-hidden">
        
        {/* html5-qrcode element mount node */}
        <div id="reader" className="absolute inset-0 w-full h-full object-cover" />

        {/* Viewfinder Overlay Frame */}
        <div className="relative w-64 h-64 z-30 flex items-center justify-center">
          
          {/* Corner brackets */}
          <div className="absolute top-0 left-0 w-8 h-8 border-t-4 border-l-4 border-primary rounded-tl-xl" />
          <div className="absolute top-0 right-0 w-8 h-8 border-t-4 border-r-4 border-primary rounded-tr-xl" />
          <div className="absolute bottom-0 left-0 w-8 h-8 border-b-4 border-l-4 border-primary rounded-bl-xl" />
          <div className="absolute bottom-0 right-0 w-8 h-8 border-b-4 border-r-4 border-primary rounded-br-xl" />

          {/* Glowing Red laser scan line */}
          <div className="absolute left-2 right-2 h-0.5 bg-red-500 shadow-[0_0_15px_rgba(239,68,68,0.8)] animate-scan-line" />

          {/* Scanner message */}
          {!cameraActive && (
            <div className="absolute text-center bg-black/75 px-4 py-3 rounded-2xl border border-white/10 backdrop-blur-md max-w-[200px]">
              <p className="text-[10px] font-bold text-textSecondary uppercase tracking-widest">Demo Mode</p>
              <p className="text-[11px] text-white font-medium mt-1 leading-snug">Webcam unavailable</p>
              <button
                onClick={() => handleMockScan()}
                className="mt-3 py-1.5 px-3.5 bg-primary/25 border border-primary/20 rounded-xl text-[9px] font-bold text-white uppercase active:scale-95 transition-transform w-full"
              >
                Scan Mock QR
              </button>
            </div>
          )}
        </div>

        {/* Instruction label */}
        <span className="absolute bottom-32 z-30 text-xs font-bold text-white/70 bg-black/40 backdrop-blur-md px-4 py-2 rounded-full border border-white/5 uppercase tracking-wider">
          Point camera at QR code
        </span>
      </div>

      {/* Floating Action footer toolbar */}
      <div className="h-24 bg-[#12121A] border-t border-white/5 flex items-center justify-around px-6 safe-bottom z-30 select-none">
        
        {/* Gallery upload */}
        <label className="flex flex-col items-center gap-1.5 cursor-pointer">
          <input
            type="file"
            accept="image/*"
            onChange={handleGalleryUpload}
            className="hidden"
          />
          <div className="w-12 h-12 rounded-2xl bg-white/5 border border-white/5 hover:bg-white/10 flex items-center justify-center text-textSecondary hover:text-white transition-colors">
            <Image className="w-5 h-5" />
          </div>
          <span className="text-[10px] font-bold text-textSecondary">Upload QR</span>
        </label>

        {/* Manual Keyboard Entry Drawer */}
        <button
          onClick={() => { triggerHaptic('light'); setIsManualSheetOpen(true); }}
          className="flex flex-col items-center gap-1.5 focus:outline-none group"
        >
          <div className="w-12 h-12 rounded-2xl bg-white/5 border border-white/5 group-hover:bg-white/10 flex items-center justify-center text-textSecondary group-hover:text-white transition-colors">
            <Keyboard className="w-5 h-5" />
          </div>
          <span className="text-[10px] font-bold text-textSecondary">Enter UPI ID</span>
        </button>
      </div>

      {/* Manual UPI Bottom Sheet */}
      <BottomSheet isOpen={isManualSheetOpen} onClose={() => setIsManualSheetOpen(false)} title="Manual Payment Entry">
        <div className="flex flex-col gap-4 my-2">
          <Input
            label="UPI ID"
            placeholder="e.g. receiver@upimesh"
            value={manualUpi}
            onChange={(e) => setManualUpi(e.target.value)}
          />
          
          <Button
            onClick={handleManualProceed}
            disabled={!manualUpi}
            variant="gradient"
            className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2"
          >
            <Check className="w-4 h-4" />
            Proceed
          </Button>
        </div>
      </BottomSheet>
    </div>
  );
};

export default ScanScreen;
