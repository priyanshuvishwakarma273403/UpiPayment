/**
 * Custom hook simulating native mobile haptic feedback.
 * Calls navigator.vibrate if available.
 */
export const useHaptic = () => {
  const triggerHaptic = (type = 'light') => {
    if (typeof window === 'undefined' || !window.navigator || !window.navigator.vibrate) {
      return;
    }

    try {
      switch (type) {
        case 'light':
          window.navigator.vibrate(15);
          break;
        case 'medium':
          window.navigator.vibrate(35);
          break;
        case 'heavy':
          window.navigator.vibrate(65);
          break;
        case 'success':
          window.navigator.vibrate([30, 50, 30]);
          break;
        case 'error':
          window.navigator.vibrate([50, 100, 50]);
          break;
        default:
          window.navigator.vibrate(20);
      }
    } catch (e) {
      console.warn("Haptic feedback error:", e);
    }
  };

  return triggerHaptic;
};

export default useHaptic;
