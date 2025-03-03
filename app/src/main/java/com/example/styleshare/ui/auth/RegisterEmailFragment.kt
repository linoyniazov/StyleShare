import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.styleshare.databinding.FragmentRegisterEmailBinding

class RegisterEmailFragment : Fragment() {

    private var _binding: FragmentRegisterEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()

            if (email.isNotEmpty()) {
                val action = RegisterEmailFragmentDirections
                    .actionRegisterEmailFragmentToRegisterPasswordFragment(email)
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}