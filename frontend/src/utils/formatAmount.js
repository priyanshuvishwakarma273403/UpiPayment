/**
 * Formats a number to Indian Rupee format (e.g., 123456.78 -> ₹1,23,456.78)
 * @param {number|string} amount 
 * @returns {string} Formatted currency string
 */
export const formatAmount = (amount) => {
  if (amount === undefined || amount === null) return '₹0.00';
  const val = typeof amount === 'string' ? parseFloat(amount) : amount;
  if (isNaN(val)) return '₹0.00';
  
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(val);
};

export default formatAmount;
