package com.erastimothy.laundry_app.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.erastimothy.laundry_app.R;
import com.erastimothy.laundry_app.adapter.LaundryRecyclerViewAdapter;
import com.erastimothy.laundry_app.adapter.OrderanLaundryAdapter;
import com.erastimothy.laundry_app.adapter.RiwayatOrderanLaundryAdapter;
import com.erastimothy.laundry_app.api.UserAPI;
import com.erastimothy.laundry_app.dao.LaundryDao;
import com.erastimothy.laundry_app.dao.UserDao;
import com.erastimothy.laundry_app.model.Laundry;
import com.erastimothy.laundry_app.model.Layanan;
import com.erastimothy.laundry_app.model.Toko;
import com.erastimothy.laundry_app.model.User;
import com.erastimothy.laundry_app.preferences.LaundryPreferences;
import com.erastimothy.laundry_app.preferences.LayananPreferences;
import com.erastimothy.laundry_app.preferences.TokoPreferences;
import com.erastimothy.laundry_app.preferences.UserPreferences;
import com.erastimothy.laundry_app.user.MyOrderActivity;
import com.erastimothy.laundry_app.user.OrderDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.android.volley.Request.Method.GET;

public class RiwayatOrder extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private SearchView searchView;
    private RiwayatOrderanLaundryAdapter adapter;
    private MaterialButton btnBack, btnGeneratePDF;
    private LaundryDao laundryDao;
    private LaundryPreferences laundryPreferences;
    private LayananPreferences layananPreferences;
    private List<Laundry> myLaundryList;
    private List<Laundry> laundryListFull;
    private List<Layanan> layananList;
    private Laundry laundry;
    private UserDao userDao;
    private TokoPreferences TokoSP;

    private int totalPesanan = 0;
    private int totalPendapatan = 0;
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
    private NumberFormat numberFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_order);
        myLaundryList = new ArrayList<>();
        userDao = new UserDao(this);

        layananPreferences = new LayananPreferences(getApplicationContext());
        layananList = layananPreferences.getListLayananFromSharedPreferences();
        TokoSP = new TokoPreferences(this);
        btnBack = findViewById(R.id.btnBack);
        btnGeneratePDF = findViewById(R.id.generatePDFbtn);
        searchView = (SearchView) findViewById(R.id.search_pegawai);
        refreshLayout = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.order_rv);

        //asign data
        laundryDao = new LaundryDao(this);
        laundryDao.setAllDataLaundry();

        laundryPreferences = new LaundryPreferences(this);
        laundryListFull = laundryPreferences.getListLaundryFromSharedPreferences();

        Locale localID = new Locale("in", "ID");
        numberFormat = NumberFormat.getCurrencyInstance(localID);

        //assign data only my order not all
        if (laundryListFull != null) {
            for (int i = 0; i < laundryListFull.size(); i++) {
                if (laundryListFull.get(i).getStatus().equalsIgnoreCase("Pesanan Selesai") || laundryListFull.get(i).getStatus().equalsIgnoreCase("Pesanan Batal")) {
                    myLaundryList.add((Laundry) laundryListFull.get(i));
                }
                if (laundryListFull.get(i).getStatus().trim().equalsIgnoreCase("Pesanan Selesai")) {
                    totalPesanan++;
                    totalPendapatan += laundryListFull.get(i).getTotal();
                }
            }
        }


        adapter = new RiwayatOrderanLaundryAdapter(this, myLaundryList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RiwayatOrder.super.onBackPressed();

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                refreshLayout.setRefreshing(false);
                return false;
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(RiwayatOrder.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(RiwayatOrder.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(RiwayatOrder.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(RiwayatOrder.this,
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


    private void createPdf() throws FileNotFoundException, DocumentException {
        /**
         * Creating Document
         */
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Download/");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(TAG, "Direktori baru untuk file pdf berhasil dibuat");
        }
        String pdfname = "laporan penjualan-" + TokoSP.getToko().getName() + ".pdf";
        pdfFile = new File(docsFolder.getAbsolutePath(), pdfname);
        OutputStream output = new FileOutputStream(pdfFile);
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
        writer = PdfWriter.getInstance(document, output);
        document.open();

        Paragraph judul = new Paragraph("LAPORAN PENJUALAN " + TokoSP.getToko().getName() + " \n\n\n", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 16, com.itextpdf.text.Font.BOLD, BaseColor.BLACK));
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

        Paragraph userInfo = new Paragraph("Total pesanan Selesai  : " +
                totalPesanan + "\n" + "Total Pendapatan  : " + numberFormat.format(totalPendapatan) + "\n",
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK)
        );
        cellSupplier.addElement(userInfo);
        tables.addCell(cellSupplier);
        PdfPCell cellPO = new PdfPCell();
//TODO 2.4 - Ubah NPM Praktikan dengan NPM anda dan ubah Tanggal Praktikum sesuai tanggal praktikum modul 11 kelas anda
        Paragraph NomorTanggal = new Paragraph(
                "Tanggal : " + java.time.LocalDate.now() + "\n",
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK)
        );
        NomorTanggal.setPaddingTop(5);
        tables.addCell(NomorTanggal);
        document.add(tables);
        com.itextpdf.text.Font f = new
                com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 14, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
        Paragraph Subjudul = new Paragraph("\nList Laundry Selesai \n\n", f);
        Subjudul.setAlignment(Element.ALIGN_CENTER);
        document.add(Subjudul);

        PdfPTable tableHeader = new PdfPTable(new float[]{1, 3, 5, 6, 4});
        tableHeader.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        tableHeader.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        //tableHeader.getDefaultCell().setFixedHeight(30);
        tableHeader.setTotalWidth(PageSize.A4.getWidth());
        tableHeader.setWidthPercentage(100);

        PdfPCell h1 = new PdfPCell(new Phrase("ID"));
        h1.setHorizontalAlignment(Element.ALIGN_CENTER);
        h1.setPaddingBottom(5);
        PdfPCell h2 = new PdfPCell(new Phrase("Tanggal"));
        h2.setHorizontalAlignment(Element.ALIGN_CENTER);
        h2.setPaddingBottom(5);
        PdfPCell h3 = new PdfPCell(new Phrase("Pelanggan"));
        h3.setHorizontalAlignment(Element.ALIGN_CENTER);
        h3.setPaddingBottom(5);
        PdfPCell h4 = new PdfPCell(new Phrase("Item"));
        h4.setHorizontalAlignment(Element.ALIGN_CENTER);
        h4.setPaddingBottom(5);
        PdfPCell h5 = new PdfPCell(new Phrase("Total"));
        h5.setHorizontalAlignment(Element.ALIGN_CENTER);
        h5.setPaddingBottom(5);

        tableHeader.addCell(h1);
        tableHeader.addCell(h2);
        tableHeader.addCell(h3);
        tableHeader.addCell(h4);
        tableHeader.addCell(h5);

        PdfPCell[] cells = tableHeader.getRow(0).getCells();
        for (int j = 0; j < cells.length; j++) {
            cells[j].setBackgroundColor(BaseColor.GRAY);
        }
        document.add(tableHeader);

        PdfPTable tableData = new PdfPTable(new float[]{1, 3, 5, 6, 4});
        tableData.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        tableData.getDefaultCell().setFixedHeight(30);
        tableData.setTotalWidth(PageSize.A4.getWidth());
        tableData.setWidthPercentage(100);
        tableData.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

        double grandtotal =0;
        int arrLength = laundryListFull.size();
        for (int x = 0; x < arrLength; x++) {
            for (int i = 0; i < cells.length; i++) {
                if (i == 0) {
                    tableData.addCell(String.valueOf(laundryListFull.get(x).getId()));
                } else if (i == 1) {
                    tableData.addCell(laundryListFull.get(x).getDate());
                } else if (i == 2) {
                    tableData.addCell(laundryListFull.get(x).getAddress());
                } else if (i == 3) {
                    String layananz="";
                    laundryListFull.get(x).getService_id();
                    for (int k =0 ;k< layananList.size();k++){
                        if(layananList.get(k).getId() == laundryListFull.get(x).getService_id());
                        {
                            layananz = layananList.get(k).getName() + "   :   " + laundryListFull.get(x).getQuantity()
                            + " x " +layananList.get(k).getHarga();
                        }
                    }
                    tableData.addCell(layananz + "\n" + "Biaya Kirim   :   " + laundryListFull.get(x).getShippingcost());
                } else if (i == 4) {
                    tableData.addCell(String.valueOf(laundryListFull.get(x).getTotal()));
                    grandtotal += laundryListFull.get(x).getTotal();
                }
            }
        }

        PdfPCell cell = new PdfPCell(new Phrase("Total"));
        cell.setColspan(4);
        tableData.addCell(cell);
        tableData.addCell(String.valueOf(numberFormat.format(grandtotal)));

        document.add(tableData);
        com.itextpdf.text.Font h = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 10, com.itextpdf.text.Font.NORMAL);
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        String tglDicetak = sdf.format(currentTime);
        Paragraph P = new Paragraph("\nDicetak tanggal " + tglDicetak, h);
        P.setAlignment(Element.ALIGN_RIGHT);
        document.add(P);

        Paragraph tokoInfo = new Paragraph("\n\n\n\n" + TokoSP.getToko().getName() + "\n\n" + TokoSP.getToko().getAlamat() +
                "\n"+ TokoSP.getToko().getTelp(), h);

        tokoInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(tokoInfo);
        document.close();
        Log.e("PDF", "SUCCESS");
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