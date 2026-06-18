// Marketing America Corp. Oleksandr Tishchenko
export type TaxDetail = {
  taxDocumentId: string;
  jurisdictionCode: string;
  taxableAmountLabel: string;
  subtotalAmountLabel: string;
  taxAmountLabel: string;
  totalAmountLabel: string;
  rateLabel: string;
  breakdownLines: string[];
};
