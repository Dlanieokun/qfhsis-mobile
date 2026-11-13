package com.android.hfsis.morbidity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.address.Barangay;
import com.android.hfsis.model.address.Municipality;
import com.android.hfsis.model.address.Province;
import com.android.hfsis.model.address.Region;
import com.android.hfsis.model.morbidity.MorbidityRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class MorbidityFragment extends Fragment {

    private static final String ARG_RECORD_ID = "record_id";
    private static final int MODE_ADD = 0;
    private static final int MODE_EDIT = 1;

    // SharedPreferences constants — same file/key used across the whole app
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_USER_ID = "user_id";

    private int mode = MODE_ADD;
    private int recordId = -1;
    private MorbidityRecord existingRecord;

    // Header fields
    private AutoCompleteTextView spinnerDisease;
    private EditText etIcdCode;
    private AutoCompleteTextView spinnerMonth;
    private AutoCompleteTextView spinnerYear;
    private AutoCompleteTextView etRegion;
    private AutoCompleteTextView etProvince;
    private AutoCompleteTextView etMunicipality;
    private AutoCompleteTextView etBarangay;

    // Cascading Address Lookup Datasets (loaded from local Room DB)
    private List<Region> regionList = new ArrayList<>();
    private List<Province> provinceList = new ArrayList<>();
    private List<Municipality> municipalityList = new ArrayList<>();
    private List<Barangay> barangayList = new ArrayList<>();

    // Currently selected parent codes, used to filter the next dropdown level
    private String selectedRegCode = null;
    private String selectedProvCode = null;
    private String selectedCitymunCode = null;

    // Age group fields - Male / Female
    private EditText et0to6dMale, et0to6dFemale;
    private EditText et7to28dMale, et7to28dFemale;
    private EditText et29dto11moMale, et29dto11moFemale;
    private EditText et1to4yrsMale, et1to4yrsFemale;
    private EditText et5to9yrsMale, et5to9yrsFemale;
    private EditText et10to14yrsMale, et10to14yrsFemale;
    private EditText et15to19yrsMale, et15to19yrsFemale;
    private EditText et20to24yrsMale, et20to24yrsFemale;
    private EditText et25to29yrsMale, et25to29yrsFemale;
    private EditText et30to34yrsMale, et30to34yrsFemale;
    private EditText et35to39yrsMale, et35to39yrsFemale;
    private EditText et40to44yrsMale, et40to44yrsFemale;
    private EditText et45to49yrsMale, et45to49yrsFemale;
    private EditText et50to54yrsMale, et50to54yrsFemale;
    private EditText et55to59yrsMale, et55to59yrsFemale;
    private EditText et60plusMale, et60plusFemale;

    // Total display fields
    private TextView tvTotalMale, tvTotalFemale, tvGrandTotal;

    private Button btnSave, btnCancel;

    private static final String[] DISEASES = {
            "Certain Infectious and Parasitic Diseases",
            "Cholera",
            "Typhoid and paratyphoid fevers",
            "Shigellosis",
            "Amoebiasis",
            "Diarrhea and gastroenteritis of presumed infectious origin",
            "Other and unspecified gastroenteritis and colitis of infectious origin; acute bloody diarrhea",
            "Other and unspecified gastroenteritis and colitis of infectious origin; acute watery diarrhea",
            "Other intestinal infectious diseases",
            "Respiratory tuberculosis",
            "Other tuberculosis",
            "Plague",
            "Brucellosis",
            "Leptospirosis",
            "Leprosy",
            "Tetanus neonatorum",
            "Other tetanus",
            "Diphtheria",
            "Whooping cough",
            "Meningococcal infection",
            "Sepsis",
            "Other bacterial diseases",
            "Congenital syphilis",
            "Early syphilis",
            "Other syphilis",
            "Gonococcal infection",
            "Sexually transmitted chlamydial diseases",
            "Other infection with a predominantly sexual mode of transmission",
            "Yaws",
            "Relapsing fevers",
            "Trachoma",
            "Typhus fever",
            "Acute poliomyelitis",
            "Rabies",
            "Viral encephalitis",
            "Yellow fever",
            "Dengue",
            "Other arthropod-borne viral fever and viral haemorrhagic fevers",
            "Herpesviral infections",
            "Varicella and zoster",
            "Measles",
            "Rubella",
            "Acute hepatitis A",
            "Acute hepatitis B",
            "Other acute viral hepatitis",
            "Other chronic and unspecified viral hepatitis",
            "Human immunodeficiency virus [HIV] disease",
            "Mumps",
            "Other viral diseases",
            "Mycoses",
            "Malaria",
            "Leishmaniasis",
            "Trypanosomiasis",
            "Schistosomiasis",
            "Other fluke infections",
            "Echinococcosis",
            "Dracunculiasis",
            "Onchocerciasis",
            "Filariasis",
            "Hookworm diseases",
            "Other helminthiases",
            "Sequelae of tuberculosis",
            "Sequelae of poliomyelitis",
            "Sequelae of leprosy",
            "Other infectious and parasitic diseases",
            "Neoplasms",
            "Malignant neoplasm of lip, oral cavity and pharynx",
            "Malignant neoplasm of oesophagus",
            "Malignant neoplasm of stomach",
            "Malignant neoplasm of colon",
            "Malignant neoplasm of rectosigmoid junction, rectum, anus and anal canal",
            "Malignant neoplasm of liver and intrahepatic bile ducts",
            "Malignant neoplasm of pancreas",
            "Other malignant neoplasms of digestive organs",
            "Malignant neoplasms of larynx",
            "Malignant neoplasm of trachea, bronchus and lung",
            "Other malignant neoplasms of respiratory and intrathoracic organs",
            "Malignant neoplasm of bone and articular cartilage",
            "Malignant melanoma of skin",
            "Other malignant neoplasms of skin",
            "Malignant neoplasms of mesothelial and soft tissue",
            "Malignant neoplasm of breast",
            "Malignant neoplasm of cervix uteri",
            "Malignant neoplasm of other and unspecified parts of uterus",
            "Other malignant neoplasms of female genital organs",
            "Malignant neoplasm of prostate",
            "Other malignant neoplasms of male genital organs",
            "Malignant neoplasm of bladder",
            "Other malignant neoplasms of urinary tract",
            "Malignant neoplasm of eye and adnexa",
            "Malignant neoplasm of brain",
            "Malignant neoplasm of other parts of central nervous system",
            "Malignant neoplasm of other, ill-defined, secondary, unspecified, and multiple sites",
            "Hodgkin disease",
            "Non-Hodgkin lymphoma",
            "Leukaemia",
            "Other malignant neoplasms of lymphoid, hematopoietic, and related tissue",
            "Carcinoma in situ of cervix uteri",
            "Benign neoplasm of skin",
            "Benign neoplasm of breast",
            "Leiomyoma of uterus",
            "Benign neoplasm of ovary",
            "Benign neoplasm of urinary organs",
            "Benign neoplasm of brain and other parts of central nervous system",
            "Other in situ and benign neoplasms and neoplasms of uncertain and unknown behavior",
            "Diseases of the blood and blood-forming organs and certain disorders involving the immune mechanism",
            "Iron deficiency anemia",
            "Other anemias",
            "Haemorrhagic conditions and other diseases of blood and blood-forming organs",
            "Certain disorders involving the immune mechanism",
            "Endocrine, nutritional and metabolic diseases",
            "Iodine-deficiency-related thyroid disorders",
            "Thyrotoxicosis",
            "Other disorders of thyroid",
            "Diabetes mellitus",
            "Malnutrition",
            "Vitamin A deficiency",
            "Other vitamin deficiencies",
            "Sequelae of malnutrition and other nutritional deficiencies",
            "Obesity",
            "Volume depletion",
            "Other endocrine, nutritional and metabolic disorders",
            "Mental and Behavioral Disorders",
            "Dementia",
            "Mental and behavioral disorders due to use of alcohol",
            "Mental and behavioral disorders due to other psychoactive substance use",
            "Schizophrenia, schizotypal, and delusional disorders",
            "Mood [affective] disorders",
            "Neurotic, stress-related, and somatoform disorders",
            "Mental retardation",
            "Other mental and behavioural disorders",
            "Diseases of the nervous system",
            "Inflammatory diseases of the central nervous system",
            "Parkinson disease",
            "Alzheimer disease",
            "Multiple sclerosis",
            "Epilepsy",
            "Migraine and other headache syndromes",
            "Transient cerebral ischaemic attacks and related syndromes",
            "Nerve, nerve root and plexus disorders",
            "Cerebral palsy and other paralytic syndromes",
            "Other diseases of the nervous system",
            "Diseases of the eye and adnexa",
            "Inflammation of eyelid",
            "Conjunctivitis and other disorders of conjunctiva",
            "Keratitis and other disorders of sclera and cornea",
            "Cataract and other disorders of lens",
            "Retinal detachments and breaks",
            "Glaucoma",
            "Strablismus",
            "Disorders of refraction and accommodation",
            "Blindness and low vision",
            "Other diseases of the eye and adnexa",
            "Diseases of the ear and mastoid process",
            "Otitis media and other disorders of middle ear and mastoid",
            "Hearing loss",
            "Other diseases of the ear and mastoid process",
            "Diseases of the circulatory system",
            "Acute rheumatic fever",
            "Chronic rheumatic heart disease",
            "Essential (primary) hypertension",
            "Other hypertensive diseases",
            "Acute myocardial infarction",
            "Other ischaemic heart diseases",
            "Pulmonary embolism",
            "Conduction disorders and cardiac arrhythmias",
            "Heart failure",
            "Other heart diseases",
            "Intracranial haemorrhage",
            "Cerebral infarction",
            "Stroke, not specified as haemorrhage or infarction",
            "Other cerebrovascular diseases",
            "Atherosclerosis",
            "Other peripheral vascular diseases",
            "Arterial embolism and thrombosis",
            "Other diseases of arteries, arterioles and capillaries",
            "Phlebitis, thrombophlebitis, venous embolism and thrombosis",
            "Varicose veins of lower extremities",
            "Other diseases of the circulatory system",
            "Diseases of the respiratory system",
            "Acute pharyngitis and acute tonsillitis",
            "Acute laryngitis and tracheitis",
            "Other acute upper respiratory infections",
            "Influenza",
            "Pneumonia",
            "Acute bronchitis and acute bronchiolitis",
            "Chronic sinusitis",
            "Other diseases of nose and nasal sinuses",
            "Chronic disease of tonsils and adenoids",
            "Other diseases of upper respiratory tract",
            "Bronchitis",
            "Emphysema",
            "Other chronic obstructive pulmonary diseases",
            "Asthma",
            "Bronchiectasis",
            "Pneumoconiosis",
            "Unspecified acute lower respiratory infection",
            "Other diseases of the respiratory system",
            "Diseases of the digestive system",
            "Dental caries",
            "Other disorders of teeth and supporting structures",
            "Other diseases of the oral cavity, salivary glands and jaws",
            "Gastric and duodenal ulcer",
            "Gastritis and duodenitis",
            "Other diseases of oesophagus, stomach and duodenum",
            "Diseases of appendix",
            "Inguinal hernia",
            "Other hernia",
            "Crohn disease and ulcerative colitis",
            "Paralytic ileus and intestinal obstruction without hernia",
            "Diverticular disease of intestine",
            "Other diseases of intestines and peritoneum",
            "Alcoholic liver disease",
            "Other diseases of liver",
            "Cholelithiasis and cholecystitis",
            "Acute pancreatitis and other diseases of the pancreas",
            "Hemorrhoids",
            "Other diseases of the digestive system",
            "Diseases of the Skin and subcutaneous tissue",
            "Infections of the skin and subcutaneous tissue",
            "Other diseases of the skin and subcutaneous",
            "Diseases of the musculoskeletal system and connective tissue",
            "Rheumatoid arthritis and other inflammatory polyarthropathies",
            "Arthrosis",
            "Acquired deformities of limbs",
            "Other disorders of joints",
            "Systemic connective tissue disorders",
            "Cervical and other intervertebral disc disorders",
            "Other dorsopathies",
            "Soft tissue disorders",
            "Disorders of bone density and structure",
            "Osteomyelitis",
            "Other diseases of the musculoskeletal system and connective tissue",
            "Diseases of the genitourinary system",
            "Acute and rapidly progressive nephritic syndromes",
            "Other glomerular diseases",
            "Renal tubulo–interstitial diseases",
            "Renal failure",
            "Urolithiasis",
            "Cystitis",
            "Other diseases of the urinary system",
            "Urinary tract infection, site not specified",
            "Hyperplasia of prostate",
            "Other disorders of prostate",
            "Hydrocele and spermatocele",
            "Redundant prepuce, phimosis and paraphimosis",
            "Other diseases of male genital organs",
            "Disorders of the breast",
            "Salpingitis and oophoritis",
            "Inflammatory disease of cervix uteri",
            "Other inflammatory diseases of female pelvic organs",
            "Endometriosis",
            "Female genital prolapse",
            "Noninflammatory disorders of ovary, fallopian tube and broad ligament",
            "Disorders of menstruation",
            "Menopausal and other perimenopausal disorders",
            "Female infertility",
            "Other disorders of genitourinary tract",
            "Pregnancy, childbirth and the puerperium",
            "Spontaneous abortion",
            "Medical abortion",
            "Other pregnancies with abortive outcome",
            "Oedema, proteinuria and hypertensive disorders in pregnancy, childbirth and the puerperium",
            "Placenta previa, premature separation of placenta and antepartum haemorrhage",
            "Other maternal care related to fetus and amniotic cavity and possible delivery problems",
            "Obstructed labour",
            "Postpartum haemorrhage",
            "Other complications of pregnancy and delivery",
            "Complications predominantly related to the puerperium and other obstetric conditions, not elsewhere classified",
            "Certain conditions originating in the perinatal period",
            "Fetus and newborn affected by maternal factors and by complications of pregnancy, labour and delivery",
            "Slow fetal growth, fetal malnutrition and disorders related to short gestation and low birth weight",
            "Birth trauma",
            "Intrauterine hypoxia and birth asphyxia",
            "Other respiratory disorders originating in the perinatal period",
            "Congenital infectious and parasitic diseases",
            "Other infections specific to the perinatal period",
            "Haemolytic disease of fetus and newborn",
            "Other conditions originating in the perinatal period",
            "Congenital Malformations, Deformations and Chromosomal Abnormalities",
            "Spina bifida",
            "Other congenital malformations of the nervous system",
            "Congenital malformations of the circulatory system",
            "Cleft lip and cleft palate",
            "Absence, atresia and stenosis of small intestine",
            "Other congenital malformations of the digestive system",
            "Undescended testicle",
            "Other malformations of the genitourinary system",
            "Congenital deformities of hip",
            "Congenital deformities of feet",
            "Other congenital malformations and deformities of the musculoskeletal system",
            "Other congenital malformations",
            "Chromosomal abnormalities, not elsewhere classified",
            "Abdominal and pelvic pain",
            "Fever of unknown origin",
            "Senility",
            "Other symptoms, signs and abnormal clinical and laboratory findings, not elsewhere classified",
            "Injury, poisoning and certain other consequences of external causes",
            "Fracture of skull and facial bones",
            "Fracture of neck, thorax or pelvis",
            "Fracture of femur",
            "Fractures of other limb bones",
            "Fractures involving multiple body regions",
            "Dislocations, sprains and strains of specified and multiple body regions",
            "Injury of eye and orbit",
            "Intracranial injury",
            "Injury of other internal organs",
            "Crushing injuries and traumatic amputations of specified and multiple body regions",
            "Other injuries of specified, unspecified and multiple body regions",
            "Open wound of unspecified body region with external cause; specifically animal bites, (e.g., bitten by rat, bitten or struck by dog, bitten or struck by other mammals)",
            "Effects of foreign body entering through natural orifice",
            "Burns and corrosions",
            "Poisoning by drugs and biological substances",
            "Toxic effects of substances chiefly nonmedicinal as to source",
            "Maltreatment syndromes",
            "Other and unspecified effects of external causes",
            "Certain early complications of trauma and complications of surgical and medical care, not elsewhere classified",
            "Sequelae of injuries, poisoning and of other consequences of external causes",
            "Codes for Special Purposes",
            "SARS",
            "Vaping Disorder",
            "COVID-19, virus identified",
            "COVID-19, virus not identified",
            "Multisystem inflammatory syndrome associated with COVID-19",
            "Need for immunization against COVID-19",
            "Other"
    };

    private static final String[] ICD_CODES = {
            "A00-B99",
            "A00",
            "A01",
            "A03",
            "A06",
            "A09",
            "A09.0",
            "A09.0",
            "A02, A04-A05, A07-A08",
            "A15-A16",
            "A17-A19",
            "A20",
            "A23",
            "A27",
            "A30",
            "A33",
            "A34-A35",
            "A36",
            "A37",
            "A39",
            "A40-A41",
            "A21-A22, A24-A28, A31-A32, A38, A42-A49",
            "A50",
            "A51",
            "A52-A53",
            "A54",
            "A55-A56",
            "A57-A64",
            "A66",
            "A68",
            "A71",
            "A75",
            "A80",
            "A82",
            "A83-A86",
            "A95",
            "A97",
            "A92-A94, A96-A99",
            "B00",
            "B01-B02",
            "B05",
            "B06",
            "B15",
            "B16",
            "B17",
            "B18",
            "B20-B24",
            "B26",
            "A81, A87-A89, B03-B04, B07-B09, B25, B27-B34",
            "B35-B49",
            "B50-B54",
            "B55",
            "B56-B57",
            "B65",
            "B66",
            "B67",
            "B72",
            "B73",
            "B74",
            "B76",
            "B68-B71, B75, B77-B83",
            "B90",
            "B91",
            "B92",
            "A65-A67, A69-A70, A74, A77-A79, B58-B64, B85-B89, B94, B99",
            "C00-D48",
            "C00-C14",
            "C15",
            "C16",
            "C18",
            "C19-C21",
            "C22",
            "C25",
            "C17, C23-C24, C26",
            "C32",
            "C33-C34",
            "C30-C31, C37-C39",
            "C40-C41",
            "C43",
            "C44",
            "C45-C49",
            "C50",
            "C53",
            "C54-C55",
            "C51-C52, C56-C58",
            "C61",
            "C60,C62-C63",
            "C67",
            "C64-C66, C68",
            "C69",
            "C71",
            "C70,C72",
            "C73-C80,C97",
            "C81",
            "C82-C85",
            "C91-C95",
            "C88-C90,C96",
            "D06",
            "D22-D23",
            "D24",
            "D25",
            "D27",
            "D30",
            "D33",
            "D00-D05,D07-D21,D26,D28-D29,D31-D32,D34-D48",
            "D50-D89",
            "D50",
            "D51-D64",
            "D65-D77",
            "D80-D89",
            "E00-E90",
            "E00-E02",
            "E05",
            "E03-E04,E06-E07",
            "E10-E14",
            "E40-E46",
            "E50",
            "E51-E56",
            "E64",
            "E66",
            "E86",
            "E15-E35,E58-E63,E65,E67-E85,E87-E90",
            "F01-F99",
            "F00-F03",
            "F10",
            "F11-F19",
            "F20-F29",
            "F30-F39",
            "F40-F48",
            "F70-F79",
            "F04-F09,F50-F69,F80-F99",
            "G00-G98",
            "G00-G09",
            "G20",
            "G30",
            "G35",
            "G40-G41",
            "G43-G44",
            "G45",
            "G50-G59",
            "G80-G83",
            "G10-G14,G21-G26,G31-G32,G36-G37,G46-G47,G60-G73,G90-G99",
            "H00-H59",
            "H00-H01",
            "H10-H13",
            "H15-H19",
            "H25-H28",
            "H33",
            "H40-H42",
            "H49-H50",
            "H52",
            "H54",
            "H02-H06, H20-H22, H30-H32, H34-H36, H43-H48, H51, H53, H55-H59",
            "H60-H95",
            "H65-H75",
            "H90-H91",
            "H60-H62, H80-H83, H92-H95",
            "I00-I99",
            "I00-I02",
            "I05-I09",
            "I10",
            "I11-I15",
            "I21-I22",
            "I20, I23-I25",
            "I26",
            "I44-I49",
            "I50",
            "I27-I43, I51-I52",
            "I60-I62",
            "I63",
            "I64",
            "I65-I69",
            "I70",
            "I73",
            "I74",
            "I71-I72, I77-I79",
            "I80-I82",
            "I83",
            "I85-I99",
            "J00-J99",
            "J02-J03",
            "J04",
            "J00-J01, J05-J06",
            "J09-J11",
            "J12-J18",
            "J20-J21",
            "J32",
            "J30-J31, J33-J34",
            "J35",
            "J36-J39",
            "J40-J42",
            "J43",
            "J44",
            "J45-J46",
            "J47",
            "J60-J65",
            "J22",
            "J22, J66-J99",
            "K00-K93",
            "K02",
            "K00-K01, K03-K08",
            "K09-K14",
            "K25-K27",
            "K29",
            "K20-K23, K28, K30-K31",
            "K35-K38",
            "K40",
            "K41-K46",
            "K50-K51",
            "K56",
            "K57",
            "K52-K55, K58-K67",
            "K70",
            "K71-K77",
            "K80-K81",
            "K85-K86",
            "K64",
            "K82-K83, K87-K93",
            "L00-L99",
            "L00-L08",
            "L10-L99",
            "M00-M99",
            "M05-M14",
            "M15-M19",
            "M20-M21",
            "M00-M03, M22-M25",
            "M30-M36",
            "M50-M51",
            "M40-M49, M53-M54",
            "M60-M79",
            "M80-M85",
            "M86",
            "M87-M99",
            "N00-N99",
            "N00-N01",
            "N02-N08",
            "N10-N16",
            "N17-N19",
            "N20-N23",
            "N30",
            "N25-N29, N31-N39",
            "N39.0",
            "N40",
            "N41-N42",
            "N43",
            "N47",
            "N44-N46, N48-N51",
            "N60-N64",
            "N70",
            "N72",
            "N71, N73-N77",
            "N80",
            "N81",
            "N83",
            "N91-N92",
            "N95",
            "N97",
            "N82, N84-N90, N93-N94, N96, N98-N99",
            "O00-O99",
            "O03",
            "O04",
            "O00-O02, O05-O08",
            "O10-O16",
            "O44-O46",
            "O30-O43, O47-O48",
            "O64-O66",
            "O72",
            "O20-O29, O60-O63, O67-O71, O73-O75, O81-O84",
            "O85-O99",
            "P00-P96",
            "P00-P04",
            "P05-P07",
            "P10-P15",
            "P20-P21",
            "P22-P28",
            "P35-P37",
            "P38-P39",
            "P55",
            "P08, P29, P50-P54, P56-P96",
            "Q00-Q99",
            "Q05",
            "Q00-Q04, Q06-Q07",
            "Q20-Q28",
            "Q35-Q37",
            "Q41",
            "Q38-Q40, Q42-Q45",
            "Q53",
            "Q50-Q52, Q54-Q64",
            "Q65",
            "Q66",
            "Q67-Q79",
            "Q10-Q18, Q30-Q34, Q80-Q89",
            "Q90-Q99",
            "R10",
            "R50",
            "R54",
            "R00-R09, R11-R49, R51-R53, R55-R99",
            "S00-T98",
            "S02",
            "S12, S22, S32, T08",
            "S72",
            "S42, S52, S62, S82, S92, T10, T12",
            "T02",
            "S03, S13, S23, S33, S43, S53, S63, S73, S83, S93, T03",
            "S05",
            "S06",
            "S26-S27, S36-S37",
            "S07-S08, S17-S18, S28, S38, S47-S48, S57-S58, S67-S68, S77-S78, S87-S88, S97-S98, T04-T05",
            "S00-S01, S04, S09-S11, S14-S16, S19-S21, S24-S25, S29-S31, S34-S35, S39-S41, S44-S46, S49-S51, S54-S56, S59-S61, S64-S66, S69-S71, S74-S76, S79-S81, S84-S86, S89-S91, S94-S96, S99, T00-T01, T06-T07, T09, T11, T13-T14",
            "T14.1, W53; T14.1, W54; T14.1, W55",
            "T15-T19",
            "T20-T32",
            "T36-T50",
            "T51-T65",
            "T74",
            "T33-T35, T66-T73, T75-T78",
            "T79-T88",
            "T90-T98",
            "U00-U49",
            "U04",
            "U07.0",
            "U07.1",
            "U07.2",
            "U10.9",
            "U11.9",
            ""
    };

    private static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    public static MorbidityFragment newInstance() {
        return new MorbidityFragment();
    }

    public static MorbidityFragment newInstanceForEdit(int recordId) {
        MorbidityFragment fragment = new MorbidityFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RECORD_ID, recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_RECORD_ID)) {
            recordId = getArguments().getInt(ARG_RECORD_ID, -1);
            if (recordId != -1) {
                mode = MODE_EDIT;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_morbidity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupDropdowns();
        setupAddressDropdowns();
        setupTotalCalculations();
        setupButtons();

        if (mode == MODE_EDIT) {
            loadExistingRecord();
        } else {
            setDefaultPeriod();
        }
    }

    private void initViews(View view) {
        spinnerDisease = view.findViewById(R.id.spinner_disease);
        etIcdCode = view.findViewById(R.id.et_icd_code);
        spinnerMonth = view.findViewById(R.id.spinner_month);
        spinnerYear = view.findViewById(R.id.spinner_year);
        etRegion = view.findViewById(R.id.et_region);
        etProvince = view.findViewById(R.id.et_province);
        etMunicipality = view.findViewById(R.id.et_municipality);
        etBarangay = view.findViewById(R.id.et_barangay);

        et0to6dMale = view.findViewById(R.id.et_0to6d_male);
        et0to6dFemale = view.findViewById(R.id.et_0to6d_female);
        et7to28dMale = view.findViewById(R.id.et_7to28d_male);
        et7to28dFemale = view.findViewById(R.id.et_7to28d_female);
        et29dto11moMale = view.findViewById(R.id.et_29dto11mo_male);
        et29dto11moFemale = view.findViewById(R.id.et_29dto11mo_female);
        et1to4yrsMale = view.findViewById(R.id.et_1to4yrs_male);
        et1to4yrsFemale = view.findViewById(R.id.et_1to4yrs_female);
        et5to9yrsMale = view.findViewById(R.id.et_5to9yrs_male);
        et5to9yrsFemale = view.findViewById(R.id.et_5to9yrs_female);
        et10to14yrsMale = view.findViewById(R.id.et_10to14yrs_male);
        et10to14yrsFemale = view.findViewById(R.id.et_10to14yrs_female);
        et15to19yrsMale = view.findViewById(R.id.et_15to19yrs_male);
        et15to19yrsFemale = view.findViewById(R.id.et_15to19yrs_female);
        et20to24yrsMale = view.findViewById(R.id.et_20to24yrs_male);
        et20to24yrsFemale = view.findViewById(R.id.et_20to24yrs_female);
        et25to29yrsMale = view.findViewById(R.id.et_25to29yrs_male);
        et25to29yrsFemale = view.findViewById(R.id.et_25to29yrs_female);
        et30to34yrsMale = view.findViewById(R.id.et_30to34yrs_male);
        et30to34yrsFemale = view.findViewById(R.id.et_30to34yrs_female);
        et35to39yrsMale = view.findViewById(R.id.et_35to39yrs_male);
        et35to39yrsFemale = view.findViewById(R.id.et_35to39yrs_female);
        et40to44yrsMale = view.findViewById(R.id.et_40to44yrs_male);
        et40to44yrsFemale = view.findViewById(R.id.et_40to44yrs_female);
        et45to49yrsMale = view.findViewById(R.id.et_45to49yrs_male);
        et45to49yrsFemale = view.findViewById(R.id.et_45to49yrs_female);
        et50to54yrsMale = view.findViewById(R.id.et_50to54yrs_male);
        et50to54yrsFemale = view.findViewById(R.id.et_50to54yrs_female);
        et55to59yrsMale = view.findViewById(R.id.et_55to59yrs_male);
        et55to59yrsFemale = view.findViewById(R.id.et_55to59yrs_female);
        et60plusMale = view.findViewById(R.id.et_60plus_male);
        et60plusFemale = view.findViewById(R.id.et_60plus_female);

        tvTotalMale = view.findViewById(R.id.tv_total_male);
        tvTotalFemale = view.findViewById(R.id.tv_total_female);
        tvGrandTotal = view.findViewById(R.id.tv_grand_total);

        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void setupDropdowns() {
        ArrayAdapter<String> diseaseAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_1, DISEASES);
        spinnerDisease.setAdapter(diseaseAdapter);
        spinnerDisease.setOnItemClickListener((parent, v, position, id) -> {
            // 'position' indexes the *filtered* dropdown list, so look the disease up by name
            String selected = (String) parent.getItemAtPosition(position);
            for (int i = 0; i < DISEASES.length && i < ICD_CODES.length; i++) {
                if (DISEASES[i].equals(selected)) {
                    etIcdCode.setText(ICD_CODES[i]);
                    break;
                }
            }
        });

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, MONTHS);
        spinnerMonth.setAdapter(monthAdapter);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, years);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void setupAddressDropdowns() {
        if (getContext() == null) return;

        etProvince.setEnabled(false);
        etMunicipality.setEnabled(false);
        etBarangay.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            regionList = db.regionDao().getAllRegions();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String[] regionNames = new String[regionList.size()];
                    for (int i = 0; i < regionList.size(); i++) regionNames[i] = regionList.get(i).regDesc;

                    ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, regionNames);
                    etRegion.setAdapter(regionAdapter);
                });
            }
        });

        etRegion.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedRegCode = findRegCodeByDesc(selectedName);

            etProvince.setText("", false);
            etMunicipality.setText("", false);
            etBarangay.setText("", false);
            selectedProvCode = null;
            selectedCitymunCode = null;
            etMunicipality.setEnabled(false);
            etBarangay.setEnabled(false);

            etProvince.setEnabled(selectedRegCode != null);
            if (selectedRegCode != null) {
                loadProvincesForRegion(selectedRegCode);
            }
        });

        etProvince.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedProvCode = findProvCodeByDesc(selectedName);

            etMunicipality.setText("", false);
            etBarangay.setText("", false);
            selectedCitymunCode = null;
            etBarangay.setEnabled(false);

            etMunicipality.setEnabled(selectedProvCode != null);
            if (selectedProvCode != null) {
                loadMunicipalitiesForProvince(selectedProvCode);
            }
        });

        etMunicipality.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedCitymunCode = findCitymunCodeByDesc(selectedName);

            etBarangay.setText("", false);
            etBarangay.setEnabled(selectedCitymunCode != null);
            if (selectedCitymunCode != null) {
                loadBarangaysForMunicipality(selectedCitymunCode);
            }
        });
    }

    private void loadProvincesForRegion(String regCode) {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            provinceList = db.provinceDao().getProvincesByRegion(regCode);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String[] provinceNames = new String[provinceList.size()];
                    for (int i = 0; i < provinceList.size(); i++) provinceNames[i] = provinceList.get(i).provDesc;

                    ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, provinceNames);
                    etProvince.setAdapter(provinceAdapter);
                });
            }
        });
    }

    private void loadMunicipalitiesForProvince(String provCode) {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            municipalityList = db.municipalityDao().getMunicipalitiesByProvince(provCode);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String[] munNames = new String[municipalityList.size()];
                    for (int i = 0; i < municipalityList.size(); i++) munNames[i] = municipalityList.get(i).citymunDesc;

                    ArrayAdapter<String> munAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, munNames);
                    etMunicipality.setAdapter(munAdapter);
                });
            }
        });
    }

    private void loadBarangaysForMunicipality(String citymunCode) {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());
            barangayList = db.barangayDao().getBarangaysByMunicipality(citymunCode);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    String[] brgyNames = new String[barangayList.size()];
                    for (int i = 0; i < barangayList.size(); i++) brgyNames[i] = barangayList.get(i).brgyDesc;

                    ArrayAdapter<String> brgyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, brgyNames);
                    etBarangay.setAdapter(brgyAdapter);
                });
            }
        });
    }

    private String findRegCodeByDesc(String desc) {
        for (Region r : regionList) {
            if (r.regDesc != null && r.regDesc.equals(desc)) return r.regCode;
        }
        return null;
    }

    private String findProvCodeByDesc(String desc) {
        for (Province p : provinceList) {
            if (p.provDesc != null && p.provDesc.equals(desc)) return p.provCode;
        }
        return null;
    }

    private String findCitymunCodeByDesc(String desc) {
        for (Municipality m : municipalityList) {
            if (m.citymunDesc != null && m.citymunDesc.equals(desc)) return m.citymunCode;
        }
        return null;
    }

    private String matchRegCodeIgnoreCase(String desc) {
        if (desc == null) return null;
        for (Region r : regionList) {
            if (r.regDesc != null && r.regDesc.trim().equalsIgnoreCase(desc.trim())) return r.regCode;
        }
        return null;
    }

    private void setupTotalCalculations() {
        View.OnFocusChangeListener listener = (v, hasFocus) -> {
            if (!hasFocus) recalculateTotals();
        };

        EditText[] allFields = {
                et0to6dMale, et0to6dFemale, et7to28dMale, et7to28dFemale,
                et29dto11moMale, et29dto11moFemale, et1to4yrsMale, et1to4yrsFemale,
                et5to9yrsMale, et5to9yrsFemale, et10to14yrsMale, et10to14yrsFemale,
                et15to19yrsMale, et15to19yrsFemale, et20to24yrsMale, et20to24yrsFemale,
                et25to29yrsMale, et25to29yrsFemale, et30to34yrsMale, et30to34yrsFemale,
                et35to39yrsMale, et35to39yrsFemale, et40to44yrsMale, et40to44yrsFemale,
                et45to49yrsMale, et45to49yrsFemale, et50to54yrsMale, et50to54yrsFemale,
                et55to59yrsMale, et55to59yrsFemale, et60plusMale, et60plusFemale
        };
        for (EditText et : allFields) {
            et.setOnFocusChangeListener(listener);
        }
    }

    private void recalculateTotals() {
        int totalMale = safeInt(et0to6dMale) + safeInt(et7to28dMale) +
                safeInt(et29dto11moMale) + safeInt(et1to4yrsMale) +
                safeInt(et5to9yrsMale) + safeInt(et10to14yrsMale) +
                safeInt(et15to19yrsMale) + safeInt(et20to24yrsMale) +
                safeInt(et25to29yrsMale) + safeInt(et30to34yrsMale) +
                safeInt(et35to39yrsMale) + safeInt(et40to44yrsMale) +
                safeInt(et45to49yrsMale) + safeInt(et50to54yrsMale) +
                safeInt(et55to59yrsMale) + safeInt(et60plusMale);

        int totalFemale = safeInt(et0to6dFemale) + safeInt(et7to28dFemale) +
                safeInt(et29dto11moFemale) + safeInt(et1to4yrsFemale) +
                safeInt(et5to9yrsFemale) + safeInt(et10to14yrsFemale) +
                safeInt(et15to19yrsFemale) + safeInt(et20to24yrsFemale) +
                safeInt(et25to29yrsFemale) + safeInt(et30to34yrsFemale) +
                safeInt(et35to39yrsFemale) + safeInt(et40to44yrsFemale) +
                safeInt(et45to49yrsFemale) + safeInt(et50to54yrsFemale) +
                safeInt(et55to59yrsFemale) + safeInt(et60plusFemale);

        tvTotalMale.setText(String.valueOf(totalMale));
        tvTotalFemale.setText(String.valueOf(totalFemale));
        tvGrandTotal.setText(String.valueOf(totalMale + totalFemale));
    }

    private int safeInt(EditText et) {
        String s = et.getText().toString().trim();
        if (TextUtils.isEmpty(s)) return 0;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    private void setupButtons() {
        String label = mode == MODE_EDIT ? "Update Record" : "Save Record";
        btnSave.setText(label);

        btnSave.setOnClickListener(v -> saveRecord());
        btnCancel.setOnClickListener(v -> {
            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setDefaultPeriod() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        spinnerMonth.setText(MONTHS[month], false);
        spinnerYear.setText(String.valueOf(year), false);
    }

    private void loadExistingRecord() {
        new Thread(() -> {
            existingRecord = DatabaseHelper.getInstance(requireContext())
                    .morbidityDao().getRecordById(recordId);
            if (existingRecord != null && isAdded()) {
                requireActivity().runOnUiThread(this::populateFields);
            }
        }).start();
    }

    private void populateFields() {
        if (existingRecord == null) return;
        spinnerDisease.setText(existingRecord.getDiseaseName(), false);
        etIcdCode.setText(existingRecord.getIcdCode());
        spinnerMonth.setText(existingRecord.getReportMonth(), false);
        spinnerYear.setText(existingRecord.getReportYear(), false);

        prePopulateAddressFields();

        setField(et0to6dMale, existingRecord.getAge0to6daysMale());
        setField(et0to6dFemale, existingRecord.getAge0to6daysFemale());
        setField(et7to28dMale, existingRecord.getAge7to28daysMale());
        setField(et7to28dFemale, existingRecord.getAge7to28daysFemale());
        setField(et29dto11moMale, existingRecord.getAge29daysto11moMale());
        setField(et29dto11moFemale, existingRecord.getAge29daysto11moFemale());
        setField(et1to4yrsMale, existingRecord.getAge1to4yrsMale());
        setField(et1to4yrsFemale, existingRecord.getAge1to4yrsFemale());
        setField(et5to9yrsMale, existingRecord.getAge5to9yrsMale());
        setField(et5to9yrsFemale, existingRecord.getAge5to9yrsFemale());
        setField(et10to14yrsMale, existingRecord.getAge10to14yrsMale());
        setField(et10to14yrsFemale, existingRecord.getAge10to14yrsFemale());
        setField(et15to19yrsMale, existingRecord.getAge15to19yrsMale());
        setField(et15to19yrsFemale, existingRecord.getAge15to19yrsFemale());
        setField(et20to24yrsMale, existingRecord.getAge20to24yrsMale());
        setField(et20to24yrsFemale, existingRecord.getAge20to24yrsFemale());
        setField(et25to29yrsMale, existingRecord.getAge25to29yrsMale());
        setField(et25to29yrsFemale, existingRecord.getAge25to29yrsFemale());
        setField(et30to34yrsMale, existingRecord.getAge30to34yrsMale());
        setField(et30to34yrsFemale, existingRecord.getAge30to34yrsFemale());
        setField(et35to39yrsMale, existingRecord.getAge35to39yrsMale());
        setField(et35to39yrsFemale, existingRecord.getAge35to39yrsFemale());
        setField(et40to44yrsMale, existingRecord.getAge40to44yrsMale());
        setField(et40to44yrsFemale, existingRecord.getAge40to44yrsFemale());
        setField(et45to49yrsMale, existingRecord.getAge45to49yrsMale());
        setField(et45to49yrsFemale, existingRecord.getAge45to49yrsFemale());
        setField(et50to54yrsMale, existingRecord.getAge50to54yrsMale());
        setField(et50to54yrsFemale, existingRecord.getAge50to54yrsFemale());
        setField(et55to59yrsMale, existingRecord.getAge55to59yrsMale());
        setField(et55to59yrsFemale, existingRecord.getAge55to59yrsFemale());
        setField(et60plusMale, existingRecord.getAge60plusMale());
        setField(et60plusFemale, existingRecord.getAge60plusFemale());

        recalculateTotals();
    }

    private void setField(EditText et, int value){
        et.setText(value > 0 ? String.valueOf(value) : "");
    }

    private void prePopulateAddressFields() {
        if (existingRecord == null || getContext() == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getDatabase(getContext().getApplicationContext());

            if (regionList.isEmpty()) regionList = db.regionDao().getAllRegions();
            String regCode = findRegCodeByDesc(existingRecord.getRegion());
            if (regCode == null) regCode = matchRegCodeIgnoreCase(existingRecord.getRegion());
            selectedRegCode = regCode;

            List<Province> provinces = (regCode != null) ? db.provinceDao().getProvincesByRegion(regCode) : new ArrayList<>();
            provinceList = provinces;
            String provCode = findProvCodeByDesc(existingRecord.getProvince());
            selectedProvCode = provCode;

            List<Municipality> municipalities = (provCode != null) ? db.municipalityDao().getMunicipalitiesByProvince(provCode) : new ArrayList<>();
            municipalityList = municipalities;
            String citymunCode = findCitymunCodeByDesc(existingRecord.getMunicipality());
            selectedCitymunCode = citymunCode;

            List<Barangay> barangays = (citymunCode != null) ? db.barangayDao().getBarangaysByMunicipality(citymunCode) : new ArrayList<>();
            barangayList = barangays;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    etRegion.setText(existingRecord.getRegion(), false);

                    String[] provinceNames = new String[provinceList.size()];
                    for (int i = 0; i < provinceList.size(); i++) provinceNames[i] = provinceList.get(i).provDesc;
                    etProvince.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, provinceNames));
                    etProvince.setEnabled(!provinceList.isEmpty());
                    etProvince.setText(existingRecord.getProvince(), false);

                    String[] munNames = new String[municipalityList.size()];
                    for (int i = 0; i < municipalityList.size(); i++) munNames[i] = municipalityList.get(i).citymunDesc;
                    etMunicipality.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, munNames));
                    etMunicipality.setEnabled(!municipalityList.isEmpty());
                    etMunicipality.setText(existingRecord.getMunicipality(), false);

                    String[] brgyNames = new String[barangayList.size()];
                    for (int i = 0; i < barangayList.size(); i++) brgyNames[i] = barangayList.get(i).brgyDesc;
                    etBarangay.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, brgyNames));
                    etBarangay.setEnabled(!barangayList.isEmpty());
                    etBarangay.setText(existingRecord.getBarangay(), false);
                });
            }
        });
    }

    /**
     * Reads the logged-in user's ID from SharedPreferences.
     * Uses getInt() to match how the rest of the app (e.g. FamilyPlanningFragment)
     * stores the value, then converts to String to satisfy MorbidityRecord.userId.
     * Returns null when no valid ID is found (sentinel value -1).
     */
    private String getLoggedInUserId() {
        if (getContext() == null) return null;
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        return userId != -1 ? String.valueOf(userId) : null;
    }

    private void saveRecord() {
        if (!validateForm()) return;

        recalculateTotals();

        MorbidityRecord record = (mode == MODE_EDIT && existingRecord != null)
                ? existingRecord : new MorbidityRecord();

        record.setDiseaseName(spinnerDisease.getText().toString().trim());
        record.setIcdCode(etIcdCode.getText().toString().trim());
        record.setReportMonth(spinnerMonth.getText().toString().trim());
        record.setReportYear(spinnerYear.getText().toString().trim());
        record.setRegion(etRegion.getText().toString().trim());
        record.setProvince(etProvince.getText().toString().trim());
        record.setMunicipality(etMunicipality.getText().toString().trim());
        record.setBarangay(etBarangay.getText().toString().trim());

        // Stamp the logged-in user's ID on every save/update so the record
        // is traceable back to the health worker who entered or last edited it,
        // exactly the same way ProfilingFragment stamps userId on HouseholdProfile.
        record.setUserId(getLoggedInUserId());

        record.setAge0to6daysMale(safeInt(et0to6dMale));
        record.setAge0to6daysFemale(safeInt(et0to6dFemale));
        record.setAge7to28daysMale(safeInt(et7to28dMale));
        record.setAge7to28daysFemale(safeInt(et7to28dFemale));
        record.setAge29daysto11moMale(safeInt(et29dto11moMale));
        record.setAge29daysto11moFemale(safeInt(et29dto11moFemale));
        record.setAge1to4yrsMale(safeInt(et1to4yrsMale));
        record.setAge1to4yrsFemale(safeInt(et1to4yrsFemale));
        record.setAge5to9yrsMale(safeInt(et5to9yrsMale));
        record.setAge5to9yrsFemale(safeInt(et5to9yrsFemale));
        record.setAge10to14yrsMale(safeInt(et10to14yrsMale));
        record.setAge10to14yrsFemale(safeInt(et10to14yrsFemale));
        record.setAge15to19yrsMale(safeInt(et15to19yrsMale));
        record.setAge15to19yrsFemale(safeInt(et15to19yrsFemale));
        record.setAge20to24yrsMale(safeInt(et20to24yrsMale));
        record.setAge20to24yrsFemale(safeInt(et20to24yrsFemale));
        record.setAge25to29yrsMale(safeInt(et25to29yrsMale));
        record.setAge25to29yrsFemale(safeInt(et25to29yrsFemale));
        record.setAge30to34yrsMale(safeInt(et30to34yrsMale));
        record.setAge30to34yrsFemale(safeInt(et30to34yrsFemale));
        record.setAge35to39yrsMale(safeInt(et35to39yrsMale));
        record.setAge35to39yrsFemale(safeInt(et35to39yrsFemale));
        record.setAge40to44yrsMale(safeInt(et40to44yrsMale));
        record.setAge40to44yrsFemale(safeInt(et40to44yrsFemale));
        record.setAge45to49yrsMale(safeInt(et45to49yrsMale));
        record.setAge45to49yrsFemale(safeInt(et45to49yrsFemale));
        record.setAge50to54yrsMale(safeInt(et50to54yrsMale));
        record.setAge50to54yrsFemale(safeInt(et50to54yrsFemale));
        record.setAge55to59yrsMale(safeInt(et55to59yrsMale));
        record.setAge55to59yrsFemale(safeInt(et55to59yrsFemale));
        record.setAge60plusMale(safeInt(et60plusMale));
        record.setAge60plusFemale(safeInt(et60plusFemale));
        record.setUpdatedAt(System.currentTimeMillis());
        record.setSynced(false);

        new Thread(() -> {
            if (mode == MODE_EDIT) {
                DatabaseHelper.getInstance(requireContext()).morbidityDao().update(record);
            } else {
                DatabaseHelper.getInstance(requireContext()).morbidityDao().insert(record);
            }
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    String msg = mode == MODE_EDIT ? "Record updated successfully" : "Record saved successfully";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        }).start();
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(spinnerDisease.getText())) {
            spinnerDisease.setError("Please select a disease");
            spinnerDisease.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(spinnerMonth.getText())) {
            spinnerMonth.setError("Please select a month");
            spinnerMonth.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(spinnerYear.getText())) {
            spinnerYear.setError("Please select a year");
            spinnerYear.requestFocus();
            return false;
        }
        return true;
    }
}