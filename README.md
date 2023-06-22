# the-one-mark-and-recapture
Aurelius Aria Baras Panyapa aureliusaria0406@gmail.com

Algoritma mark and recapture dapat ditemukan di package routing.mark_and recapture, terdapat beberapa kelas pada package tersebut

Kelas Observer berisi prefix yang berfungsi sebagai identitas untuk mengindentifikasi sebuah node(terutama observer)

Interface Observer di implement oleh kelas SprayAndWaitDE_MnR yang nantinya berguna untuk report hasil

Kelas SprayAndWaitRouterDE_MnR adalah kelas routing spray and wait dengan decision engine yang ditambahkan algoritma mark and recapture(kelas utama pada TA saya)

Report yang digunakan untuk melihat hasil algoritma mark and recapture adalah kelas ReportEstimation, ReportEstimation_New pada package report
Kelas TotalNodeHidupReport digunakan untuk melihat berapa node hidup per interval waktu
Kelas interface Observer digunakan untuk mengambil nilai hasil estimasi dari kelas utama

Pada default setting terdapat beberapa skenario untuk dataset tanpa Mark and Recapture & dataset menggunakan Mark and recapture dengan skenario beberapa node mati

untuk running pergerakan random menggunakan Run_Mark&Recapture_Random_1.txt

untuk running pergerakan Reality menggunakan RealityActiveTimes-Mark&Recapture.txt

untuk running pergerakan Haggle menggunakan Haggle3ActiveTimes-Mark&Recapture.txt
