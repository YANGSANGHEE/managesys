const XLSX = require('xlsx');
const fs = require('fs');
const path = 'c:/Users/tkdgm/Downloads/고객상품(202603022120).xls';
const wb = XLSX.readFile(path, { type: 'binary' });
const ws = wb.Sheets[wb.SheetNames[0]];
const data = XLSX.utils.sheet_to_json(ws, { header: 1, defval: '', raw: false });
const headers = data[0] || [];
// Try to get column count from range
const range = XLSX.utils.decode_range(ws['!ref'] || 'A1');
const colCount = range.e.c - range.s.c + 1;
fs.writeFileSync('excel-headers.json', JSON.stringify({ colCount, headers, sampleRow: data[1] }, null, 2), 'utf8');
console.log('Columns:', colCount);
console.log('First 5 headers:', headers.slice(0, 5));
