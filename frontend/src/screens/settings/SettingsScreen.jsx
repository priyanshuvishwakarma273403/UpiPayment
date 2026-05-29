import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Volume2, Languages, Eye, Bell, Moon, ShieldCheck, ArrowLeft, Info, HelpCircle } from 'lucide-react';
import toast from 'react-hot-toast';

import { useHaptic } from '../../hooks/useHaptic';
import TopBar from '../../components/layout/TopBar';
import Card from '../../components/ui/Card';

export const SettingsScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();

  // Settings states
  const [darkMode, setDarkMode] = useState(true);
  const [language, setLanguage] = useState('en'); // 'en' or 'hi'
  const [notifications, setNotifications] = useState(true);
  const [soundVibe, setSoundVibe] = useState(true);
  const [privacyMode, setPrivacyMode] = useState(false);

  const toggleSetting = (setter, val, label) => {
    triggerHaptic('light');
    setter(!val);
    toast.success(`${label} updated!`);
  };

  const changeLanguage = (lang) => {
    triggerHaptic('medium');
    setLanguage(lang);
    toast.success(`Language set to ${lang === 'en' ? 'English' : 'Hindi (हिंदी)'}`);
  };

  const settingsOptions = [
    {
      title: 'Preferences',
      items: [
        {
          label: 'Dark Theme Mode',
          desc: 'Keep battery usage optimal',
          icon: Moon,
          action: () => toggleSetting(setDarkMode, darkMode, 'Theme'),
          value: darkMode
        },
        {
          label: 'Push Notifications',
          desc: 'Get transaction alerts immediately',
          icon: Bell,
          action: () => toggleSetting(setNotifications, notifications, 'Notifications'),
          value: notifications
        },
        {
          label: 'Sound & Vibration feedback',
          desc: 'Feel tactile feedback on buttons',
          icon: Volume2,
          action: () => toggleSetting(setSoundVibe, soundVibe, 'Audio Haptic'),
          value: soundVibe
        }
      ]
    },
    {
      title: 'Languages',
      items: [
        {
          label: 'Language Preference',
          desc: 'Select interface translation rules',
          icon: Languages,
          customElement: (
            <div className="flex bg-white/5 border border-white/5 p-0.5 rounded-lg text-[10px]">
              <button
                onClick={() => changeLanguage('en')}
                className={`px-3 py-1 rounded-md font-bold uppercase transition-colors ${
                  language === 'en' ? 'bg-primary text-white' : 'text-textSecondary'
                }`}
              >
                EN
              </button>
              <button
                onClick={() => changeLanguage('hi')}
                className={`px-3 py-1 rounded-md font-bold transition-colors ${
                  language === 'hi' ? 'bg-primary text-white' : 'text-textSecondary'
                }`}
              >
                हिन्दी
              </button>
            </div>
          )
        }
      ]
    },
    {
      title: 'Security & Privacy',
      items: [
        {
          label: 'Incognito Mode',
          desc: 'Blur balance card figures by default',
          icon: Eye,
          action: () => toggleSetting(setPrivacyMode, privacyMode, 'Privacy defaults'),
          value: privacyMode
        }
      ]
    }
  ];

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
      <TopBar title="Application Settings" />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col gap-6">
        
        {/* Settings options loops */}
        <div className="flex flex-col gap-6">
          {settingsOptions.map((section, idx) => (
            <div key={idx} className="flex flex-col gap-3">
              <h4 className="text-[10px] font-bold text-textSecondary uppercase tracking-widest text-left pl-1">
                {section.title}
              </h4>
              
              <div className="flex flex-col gap-2">
                {section.items.map((item, itemIdx) => {
                  const Svg = item.icon;
                  return (
                    <div
                      key={itemIdx}
                      className="p-4 rounded-2xl bg-white/[0.01] border border-white/[0.04] flex items-center justify-between"
                    >
                      <div className="flex items-center gap-3.5 text-left">
                        <div className="p-2 bg-white/5 rounded-xl border border-white/5 text-textSecondary">
                          <Svg className="w-4.5 h-4.5" />
                        </div>
                        <div className="flex flex-col">
                          <span className="text-xs font-bold text-white tracking-wide">{item.label}</span>
                          <span className="text-[9px] text-textSecondary mt-0.5 leading-snug">{item.desc}</span>
                        </div>
                      </div>

                      <div>
                        {item.customElement ? (
                          item.customElement
                        ) : (
                          /* Custom switch button style */
                          <button
                            type="button"
                            onClick={item.action}
                            className={`w-10 h-6 rounded-full transition-colors relative flex items-center px-0.5 focus:outline-none ${
                              item.value ? 'bg-primary' : 'bg-white/10'
                            }`}
                          >
                            <div
                              className={`w-5 h-5 rounded-full bg-white shadow-md transform transition-transform duration-100 ${
                                item.value ? 'translate-x-4' : 'translate-x-0'
                              }`}
                            />
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>

        {/* Application details footer */}
        <div className="mt-auto py-4 text-center flex flex-col items-center gap-1.5 opacity-40">
          <Info className="w-5 h-5" />
          <span className="text-[10px] font-bold tracking-widest text-textSecondary uppercase">
            UPI Mesh Client
          </span>
          <span className="text-[9px] font-medium text-textSecondary amount-font">
            Version 1.4.2 (Production Sandbox build)
          </span>
        </div>
      </div>
    </div>
  );
};

export default SettingsScreen;
