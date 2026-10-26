package pos;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Aplikasi Kasir Sederhana (POS - Point of Sale) Versi Terminal / Console.
 * Dibuat khusus untuk Kemudahan Penjelasan UTS / Live Coding.
 * 
 * FITUR UNGGULAN:
 * 1. Berjalan di Terminal / Command Line via Scanner.
 * 2. Mengkonversi input singkat (misal "10k", "15rb", "1.5jt") menjadi nominal murni.
 * 3. Format output Rupiah otomatis (Rp 10.000).
 * 4. Tampilan tabel daftar barang & Struk Transaksi yang rapi.
 * 5. Penanganan error input yang aman dengan try-catch & validasi.
 */
public class Pos {

    // --- Inner Class: Model Data Barang ---
    static class Item {
        private String nama;
        private long harga;
        private int jumlah;

        public Item(String nama, long harga, int jumlah) {
            this.nama = nama;
            this.harga = harga;
            this.jumlah = jumlah;
        }

        public String getNama() { return nama; }
        public long getHarga() { return harga; }
        public int getJumlah() { return jumlah; }
        public long getSubtotal() { return harga * jumlah; }
    }

    // List Keranjang Belanjaan
    private static List<Item> keranjang = new ArrayList<>();
    
    // Scanner global untuk membaca input terminal
    private static Scanner scanner = new Scanner(System.in);

    // Formatter resmi mata uang Indonesia (Rp)
    private static final NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // --- MAIN METHOD ---
    public static void main(String[] args) {
        boolean berjalan = true;

        while (berjalan) {
            tampilkanHeader();
            System.out.println("====== MENU KASIR ======");
            System.out.println("1. Tambah Barang ke Keranjang");
            System.out.println("2. Lihat Keranjang Belanja");
            System.out.println("3. Proses Pembayaran & Cetak Struk");
            System.out.println("4. Reset Transaksi Baru");
            System.out.println("0. Keluar Aplikasi");
            System.out.print("Pilih menu (0-4): ");

            String pilihan = scanner.nextLine().trim();
            System.out.println();

            switch (pilihan) {
                case "1":
                    tambahBarang();
                    break;
                case "2":
                    tampilkanKeranjang();
                    break;
                case "3":
                    prosesPembayaran();
                    break;
                case "4":
                    resetTransaksi();
                    break;
                case "0":
                    berjalan = false;
                    System.out.println("Terima kasih telah menggunakan Sistem Kasir POS!");
                    break;
                default:
                    System.out.println("⚠️ Pilihan tidak valid! Silakan masukkan angka 0 - 4.\n");
            }
        }
    }

    // --- LOGIKA 1: MENAMBAH BARANG KE KERANJANG ---
    private static void tambahBarang() {
        System.out.println("--- INPUT BARANG ---");
        System.out.print("Masukkan Nama Barang: ");
        String nama = scanner.nextLine().trim();

        if (nama.isEmpty()) {
            System.out.println("⚠️ Nama barang tidak boleh kosong!\n");
            return;
        }

        long harga = 0;
        while (true) {
            System.out.print("Masukkan Harga Satuan (Dukung: 10k, 15rb, 1.5jt): ");
            String inputHarga = scanner.nextLine();
            try {
                harga = parseNominalShorthand(inputHarga);
                if (harga <= 0) {
                    System.out.println("⚠️ Harga harus lebih besar dari 0!");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Format harga tidak valid! Contoh valid: 10k, 15rb, 50000");
            }
        }

        int jumlah = 0;
        while (true) {
            System.out.print("Masukkan Jumlah Qty: ");
            String inputJumlah = scanner.nextLine();
            try {
                jumlah = Integer.parseInt(inputJumlah.trim());
                if (jumlah <= 0) {
                    System.out.println("⚠️ Jumlah Qty harus lebih besar dari 0!");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Masukkan angka bulat yang valid untuk Qty!");
            }
        }

        // Simpan ke Keranjang
        Item itemBaru = new Item(nama, harga, jumlah);
        keranjang.add(itemBaru);

        System.out.println("\n✅ Berhasil menambahkan " + nama + " x" + jumlah + " (" + formatRupiah.format(itemBaru.getSubtotal()) + ")");
        System.out.println("--------------------------------------------------\n");
    }

    // --- LOGIKA 2: MENAMPILKAN KERANJANG BELANJA ---
    private static void tampilkanKeranjang() {
        if (keranjang.isEmpty()) {
            System.out.println("🛒 Keranjang belanjaan masih kosong.\n");
            return;
        }

        System.out.println("=========================================================");
        System.out.println("                  DAFTAR KERANJANG BELANJA                ");
        System.out.println("=========================================================");
        System.out.printf("%-4s | %-18s | %-12s | %-4s | %-12s%n", "No", "Nama Barang", "Harga", "Qty", "Subtotal");
        System.out.println("---------------------------------------------------------");

        long total = 0;
        for (int i = 0; i < keranjang.size(); i++) {
            Item item = keranjang.get(i);
            total += item.getSubtotal();
            System.out.printf("%-4d | %-18s | %-12s | %-4d | %-12s%n", 
                (i + 1), 
                item.getNama(), 
                formatRupiah.format(item.getHarga()), 
                item.getJumlah(), 
                formatRupiah.format(item.getSubtotal())
            );
        }

        System.out.println("---------------------------------------------------------");
        System.out.printf("%-43s : %s%n", "TOTAL BELANJA", formatRupiah.format(total));
        System.out.println("=========================================================\n");
    }

    // --- LOGIKA 3: PROSES PEMBAYARAN & STRUK ---
    private static void prosesPembayaran() {
        if (keranjang.isEmpty()) {
            System.out.println("⚠️ Keranjang masih kosong! Tambahkan barang terlebih dahulu.\n");
            return;
        }

        long totalBelanja = 0;
        for (Item item : keranjang) {
            totalBelanja += item.getSubtotal();
        }

        tampilkanKeranjang();

        long uangBayar = 0;
        while (true) {
            System.out.print("Masukkan Uang Pembayaran (Dukung: 50k, 100rb, dll): ");
            String inputBayar = scanner.nextLine();
            try {
                uangBayar = parseNominalShorthand(inputBayar);
                if (uangBayar < totalBelanja) {
                    long kurang = totalBelanja - uangBayar;
                    System.out.println("⚠️ Uang pembayaran kurang sebesar: " + formatRupiah.format(kurang));
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Nominal tidak valid! Contoh: 50k, 100rb, 100000");
            }
        }

        long kembalian = uangBayar - totalBelanja;

        // Cetak Struk Pembayaran versi Terminal
        System.out.println("\n\n=========================================================");
        System.out.println("                     STRUK PEMBAYARAN                    ");
        System.out.println("=========================================================");
        for (Item item : keranjang) {
            System.out.printf("%-20s x%-3d @%-10s = %s%n",
                item.getNama(),
                item.getJumlah(),
                formatRupiah.format(item.getHarga()),
                formatRupiah.format(item.getSubtotal())
            );
        }
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-30s : %s%n", "Total Belanja", formatRupiah.format(totalBelanja));
        System.out.printf("%-30s : %s%n", "Uang Pembayaran", formatRupiah.format(uangBayar));
        System.out.printf("%-30s : %s%n", "Kembalian", formatRupiah.format(kembalian));
        System.out.println("=========================================================");
        System.out.println("          TRANSAKSI BERHASIL - TERIMA KASIH!             ");
        System.out.println("=========================================================\n");

        // Otomatis bersihkan keranjang setelah pembayaran berhasil
        keranjang.clear();
    }

    // --- LOGIKA 4: RESET TRANSAKSI ---
    private static void resetTransaksi() {
        keranjang.clear();
        System.out.println("🔄 Transaksi berhasil direset. Keranjang sekarang kosong.\n");
    }

    // --- PARSER NOMINAL SINGKAT (10k, 15rb, 1.5jt, dll) ---
    private static long parseNominalShorthand(String text) throws NumberFormatException {
        if (text == null) return 0;
        
        String clean = text.trim().toLowerCase()
                .replace("rp", "")
                .replace(" ", "");

        if (clean.isEmpty()) return 0;

        double pengali = 1;

        if (clean.endsWith("rb")) {
            pengali = 1000;
            clean = clean.substring(0, clean.length() - 2);
        } else if (clean.endsWith("k")) {
            pengali = 1000;
            clean = clean.substring(0, clean.length() - 1);
        } else if (clean.endsWith("jt")) {
            pengali = 1000000;
            clean = clean.substring(0, clean.length() - 2);
        } else if (clean.endsWith("m") || clean.endsWith("juta")) {
            pengali = 1000000;
            clean = clean.substring(0, clean.length() - 1);
        } else {
            clean = clean.replace(".", "").replace(",", "");
        }

        clean = clean.replace(",", ".");

        double nominal = Double.parseDouble(clean) * pengali;
        return (long) nominal;
    }

    // --- DEKORASI HEADER TERMINAL ---
    private static void tampilkanHeader() {
        System.out.println("*********************************************************");
        System.out.println("          SISTEM KASIR (POINT OF SALE) TERMINAL          ");
        System.out.println("*********************************************************");
    }
}