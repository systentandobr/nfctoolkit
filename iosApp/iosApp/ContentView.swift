import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        VStack(spacing: 20) {
            Text("NFC Toolkit")
                .font(.largeTitle)
                .foregroundColor(.white)

            Button(action: {
                NFCManager.shared.startScanning { tag in
                    print("Tag detected: \(tag)")
                }
            }) {
                Text("GRAVAR")
                    .font(.title2.bold())
                    .foregroundColor(.white)
                    .padding(.vertical, 24)
                    .padding(.horizontal, 48)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [Color.teal, Color.green]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(Capsule())
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.background)
    }
}

#Preview {
    ContentView()
}

extension Color {
    static let background = Color(red: 10/255, green: 26/255, blue: 26/255)
    static let teal = Color(red: 20/255, green: 184/255, blue: 166/255)
    static let green = Color(red: 34/255, green: 197/255, blue: 94/255)
}
