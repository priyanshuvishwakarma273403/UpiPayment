/**
 * Validates a UPI ID string format (e.g. mobile@upi, user@sbi)
 * @param {string} upiId 
 * @returns {boolean}
 */
export const validateUpi = (upiId) => {
  if (!upiId) return false;
  // Format matching alphanumeric/dots/hyphens followed by @ and standard bank codes
  const upiRegex = /^[a-zA-Z0-9.\-_]{2,64}@[a-zA-Z]{2,10}$/;
  return upiRegex.test(upiId);
};

export default validateUpi;
