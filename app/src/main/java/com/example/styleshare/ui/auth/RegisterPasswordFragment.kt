import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.styleshare.databinding.FragmentRegisterPasswordBinding

class RegisterPasswordFragment : Fragment() {

    private var _binding: FragmentRegisterPasswordBinding? = null
    private val binding get() = _binding!!
    private val args: RegisterPasswordFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = args.email
        binding.emailTextView.text = email

        binding.registerButton.setOnClickListener {
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.passwordConfirmEditText.text.toString().trim()

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            findNavController().navigate(R.id.action_registerPasswordFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}