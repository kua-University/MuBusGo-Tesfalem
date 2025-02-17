import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.muuniversitybus.R
import com.example.muuniversitybus.Route

class RouteAdapter(
    private var routes: List<Route>,   // Store routes
    private val onItemClick: (Route) -> Unit  // Click listener
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    fun updateRoutes(newRoutes: List<Route>) {
        routes = newRoutes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.route_cards, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        holder.bind(route)
        holder.itemView.setOnClickListener {
            Log.d("RouteAdapter", "Clicked route: ${route.id}") // Debug log
            onItemClick(route)
        }
    }  // **Missing closing brace added here**

    override fun getItemCount(): Int = routes.size

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textRouteName: TextView = itemView.findViewById(R.id.textRouteName)
        private val imageRoute: ImageView = itemView.findViewById(R.id.imageRoute)

        fun bind(route: Route) {
            textRouteName.text = route.name
            imageRoute.setImageResource(route.imageResId)  // Load image from drawable
        }
    }
}
