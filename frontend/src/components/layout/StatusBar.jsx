import React, { useState, useEffect } from 'react';
import { Wifi, Signal, Battery } from 'lucide-react';

export const StatusBar = () => {
  const [time, setTime] = useState('');

  useEffect(() => {
    const updateTime = () => {
      const date = new Date();
      let hours = date.getHours();
      const minutes = date.getMinutes();
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12;
      hours = hours ? hours : 12; // the hour '0' should be '12'
      const minutesStr = minutes < 10 ? '0' + minutes : minutes;
      setTime(`${hours}:${minutesStr} ${ampm}`);
    };

    updateTime();
    const interval = setInterval(updateTime, 1000 * 30); // update every 30s
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="h-10 px-6 bg-[#0A0A0F]/90 backdrop-blur-md flex justify-between items-center text-[12px] font-medium tracking-tight text-white select-none border-b border-white/[0.03] z-50">
      {/* Time */}
      <span className="font-semibold">{time || '10:00 AM'}</span>
      
      {/* Icons */}
      <div className="flex items-center gap-1.5 opacity-85">
        <Signal className="w-3.5 h-3.5 fill-current" />
        <Wifi className="w-3.5 h-3.5" />
        <div className="flex items-center gap-0.5">
          <Battery className="w-4 h-4 text-white" />
        </div>
      </div>
    </div>
  );
};

export default StatusBar;
