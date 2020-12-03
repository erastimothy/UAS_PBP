package com.erastimothy.laundry_app.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.erastimothy.laundry_app.MainActivity;
import com.erastimothy.laundry_app.R;
import com.erastimothy.laundry_app.model.Laundry;
import com.erastimothy.laundry_app.model.Toko;
import com.erastimothy.laundry_app.preferences.TokoPreferences;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.WriterException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView orderid_tv,namaTv,alamatTv,tanggalTv,statusTv,jenisTv,kuantitasTv,hargaTv,totalTv,biayaAntarTv,namaTokoTv,alamatTokoTv;
    private ImageView qr_iv;
    private MaterialButton btnGeneratePDF;

    private static final String TAG = "PdfCreatorActivity";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 101;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private File pdfFile;
    private PdfWriter writer;
    private AlertDialog.Builder builder;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TokoPreferences tokoPreferences = new TokoPreferences(this);
        Toko toko = tokoPreferences.getToko();
        super.onCreate(savedInstanceState);

        Locale localID = new Locale("in","ID");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(localID);

        //set content view
        setContentView(R.layout.activity_order_detail);
        Bundle bundle = getIntent().getBundleExtra("laundry");
        qr_iv = findViewById(R.id.qr_iv);
        orderid_tv = findViewById(R.id.orderid_tv);
        namaTv = findViewById(R.id.nama_tv);
        namaTokoTv = findViewById(R.id.namaToko_tv);
        alamatTokoTv = findViewById(R.id.alamatToko_tv);
        alamatTv = findViewById(R.id.alamat_tv);
        tanggalTv = findViewById(R.id.tanggal_tv);
        statusTv = findViewById(R.id.status_tv);
        jenisTv = findViewById(R.id.jenis_tv);
        kuantitasTv = findViewById(R.id.kuantitas_tv);
        hargaTv = findViewById(R.id.harga_tv);
        totalTv = findViewById(R.id.total_tv);
        biayaAntarTv = findViewById(R.id.biaya_antar_tv);
        MaterialButton btnBack = findViewById(R.id.btnBack);
        btnGeneratePDF = findViewById(R.id.generatePDFbtn);


        //set data
        orderid_tv.setText("#"+bundle.getString("order_id"));
        namaTv.setText(bundle.getString("nama"));
        alamatTv.setText(bundle.getString("alamat"));
        tanggalTv.setText(bundle.getString("tanggal"));
        statusTv.setText(bundle.getString("status"));
        jenisTv.setText(bundle.getString("jenis"));
        kuantitasTv.setText(bundle.getString("kuantitas"));
        hargaTv.setText(numberFormat.format(Double.parseDouble(bundle.getString("harga"))));
        biayaAntarTv.setText(numberFormat.format(Double.parseDouble(bundle.getString("biaya_antar"))));
        totalTv.setText(numberFormat.format(Double.parseDouble(bundle.getString("total_pembayaran"))));
        namaTokoTv.setText(toko.getName());
        alamatTokoTv.setText(toko.getAlamat()+"\n"+toko.getTelp());



        //set qr code
        QRGEncoder qrgEncoder = new QRGEncoder(bundle.getString("order_id").trim(),null, QRGContents.Type.TEXT,150);
        try {
            Bitmap qrBits = qrgEncoder.getBitmap();
            qr_iv.setImageBitmap(qrBits);
        }catch (Exception e){
            Log.v("QR : ",e.toString());
        }


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getIntent().hasExtra("from")){
                    startActivity(new Intent(OrderDetailActivity.this, MainActivity.class));
                }
                else
                    OrderDetailActivity.super.onBackPressed();
            }
        });

        btnGeneratePDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                try {
                    createPdfWrapper();
                    createPdf();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(OrderDetailActivity.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(OrderDetailActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(OrderDetailActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(OrderDetailActivity.this,
                    PERMISSIONS_STORAGE,
                    requestCode);

        }
    }

    private void createPdfWrapper() throws FileNotFoundException, DocumentException {
        //isikan code createPdfWrapper()
        int hasWriteStoragePermission = 0;
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("Izinkan aplikasi untuk akses penyimpanan?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(PERMISSIONS_STORAGE, REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        } else {
            createPdf();
        }

    }



    private void createPdf() throws FileNotFoundException, DocumentException{
        /**
         * Creating Document
         */
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Download/");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(TAG, "Direktori baru untuk file pdf berhasil dibuat");
        }
        String pdfname = "invoice-"+orderid_tv.getText().toString()+"-"+namaTokoTv.getText().toString() + ".pdf";
        pdfFile = new File(docsFolder.getAbsolutePath(), pdfname);
        OutputStream output = new FileOutputStream(pdfFile);
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
        writer = PdfWriter.getInstance(document, output);
        document.open();

        Paragraph judul = new Paragraph("INVOICE ORDER LAUNDRY \n\n\n", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 16, com.itextpdf.text.Font.BOLD, BaseColor.BLACK));
        judul.setAlignment(Element.ALIGN_CENTER);
        document.add(judul);
        PdfPTable tables = new PdfPTable(new float[]{16, 8});
        tables.getDefaultCell().setFixedHeight(50);
        tables.setTotalWidth(PageSize.A4.getWidth());
        tables.setWidthPercentage(100);
        tables.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        PdfPCell cellSupplier = new PdfPCell();
        cellSupplier.setPaddingLeft(20);
        cellSupplier.setPaddingBottom(10);
        cellSupplier.setBorder(Rectangle.NO_BORDER);

        Paragraph userInfo = new Paragraph(
                namaTv.getText().toString()+"\n" + alamatTv.getText().toString() + "\n",
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK)
        );
        cellSupplier.addElement(userInfo);
        tables.addCell(cellSupplier);
        PdfPCell cellPO = new PdfPCell();
//TODO 2.4 - Ubah NPM Praktikan dengan NPM anda dan ubah Tanggal Praktikum sesuai tanggal praktikum modul 11 kelas anda
        Paragraph NomorTanggal = new Paragraph("Status : " + statusTv.getText().toString() + "\n\n" +
                "Tanggal : " + tanggalTv.getText().toString() + "\n",
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK)
        );
        NomorTanggal.setPaddingTop(5);
        tables.addCell(NomorTanggal);
        document.add(tables);
        com.itextpdf.text.Font f = new
                com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 14, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
        Paragraph Subjudul = new Paragraph("\nItem \n\n", f);
        Subjudul.setAlignment(Element.ALIGN_CENTER);
        document.add(Subjudul);

        PdfPTable tableHeader = new PdfPTable(new float[]{5, 2, 5});
        tableHeader.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        tableHeader.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        tableHeader.getDefaultCell().setFixedHeight(30);
        tableHeader.setTotalWidth(PageSize.A4.getWidth());
        tableHeader.setWidthPercentage(100);

        PdfPCell h1 = new PdfPCell(new Phrase("Layanan"));
        h1.setHorizontalAlignment(Element.ALIGN_CENTER);
        h1.setPaddingBottom(5);
        PdfPCell h2 = new PdfPCell(new Phrase("Kuantitas"));
        h2.setHorizontalAlignment(Element.ALIGN_CENTER);
        h2.setPaddingBottom(5);
        PdfPCell h4 = new PdfPCell(new Phrase("Harga satuan"));
        h4.setHorizontalAlignment(Element.ALIGN_CENTER);
        h4.setPaddingBottom(5);
        tableHeader.addCell(h1);
        tableHeader.addCell(h2);
        tableHeader.addCell(h4);
        PdfPCell[] cells = tableHeader.getRow(0).getCells();
        for (int j = 0; j < cells.length; j++) {
            cells[j].setBackgroundColor(BaseColor.GRAY);
        }
        document.add(tableHeader);
        PdfPTable tableData = new PdfPTable(new float[]{5, 2, 5});
        tableData.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        tableData.getDefaultCell().setFixedHeight(30);
        tableData.setTotalWidth(PageSize.A4.getWidth());
        tableData.setWidthPercentage(100);
        tableData.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

        tableData.addCell(jenisTv.getText().toString());
        tableData.addCell(kuantitasTv.getText().toString());
        tableData.addCell(hargaTv.getText().toString());

        tableData.addCell("Biaya Antar");
        tableData.addCell("");
        tableData.addCell(biayaAntarTv.getText().toString());

        PdfPCell cell = new PdfPCell(new Phrase("Total"));
        cell.setColspan(2);
        tableData.addCell(cell);
        tableData.addCell(totalTv.getText().toString());

        document.add(tableData);
        com.itextpdf.text.Font h = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.NORMAL);
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        String tglDicetak = sdf.format(currentTime);
        Paragraph P = new Paragraph("\nDicetak tanggal " + tglDicetak, h);
        P.setAlignment(Element.ALIGN_RIGHT);
        document.add(P);

        Paragraph tokoInfo = new Paragraph("\n\n\n\n"+namaTokoTv.getText().toString()+ "\n\n" + alamatTokoTv.getText().toString() +
                "\n",h);

        tokoInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(tokoInfo);
        document.close();
        Log.e("PDF","SUCCESS");
        previewPdf();

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getApplicationContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void previewPdf() {

        //isikan code previewPdf()
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfFile);
            } else {
                uri = Uri.fromFile(pdfFile);
            }
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(uri, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pdfIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            pdfIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            //TODO 2.6 - Sesuaikan package dengan package yang anda buat
            getApplicationContext().grantUriPermission("com.erastimothy.laundry_app.user", uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(pdfIntent);
        } else {
            Toast.makeText(this, "Unduh pembuka PDF untuk menampilkan file ini", Toast.LENGTH_SHORT).show();
        }

    }
}