package Phonedroid.apk;


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.app.ListActivity;

public class PhoneLogView extends ListActivity {
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);

       // Use an existing ListAdapter that will map an array
       // of strings to TextViews
       setListAdapter(new ArrayAdapter<String>(this,
               android.R.layout.simple_list_item_1, mStrings));
       getListView().setTextFilterEnabled(true);
   }
   private String[] mStrings = {
           "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
           "Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale",
           "Aisy Cendre", "Allgauer Emmentaler", "Alverca", "Ambert", "American Cheese",
           "Ami du Chambertin", "Anejo Enchilado", "Anneau du Vic-Bilh", "Anthoriro", "Appenzell",
           "Aragon", "Ardi Gasna", "Ardrahan", "Armenian String", "Aromes au Gene de Marc",
           "Xanadu", "Xynotyro", "Yarg Cornish", "Yarra Valley Pyramid", "Yorkshire Blue",
           "Zamorano", "Zanetti Grana Padano", "Zanetti Parmigiano Reggiano"};
}
