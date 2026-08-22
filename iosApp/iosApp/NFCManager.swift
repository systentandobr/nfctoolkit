import Foundation
import CoreNFC

class NFCManager: NSObject, ObservableObject, NFCNDEFReaderSessionDelegate {

    static let shared = NFCManager()
    private var session: NFCNDEFReaderSession?
    private var onTagDetected: ((String) -> Void)?

    func startScanning(onTagDetected: @escaping (String) -> Void) {
        guard NFCNDEFReaderSession.readingAvailable else {
            onTagDetected("NFC not available on this device")
            return
        }
        self.onTagDetected = onTagDetected
        session = NFCNDEFReaderSession(delegate: self, queue: nil, invalidateAfterFirstRead: false)
        session?.alertMessage = "Aproxime o iPhone da tag NFC"
        session?.begin()
    }

    func stopScanning() {
        session?.invalidate()
        session = nil
    }

    func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        for message in messages {
            for record in message.records {
                let payload = String(data: record.payload, encoding: .utf8) ?? ""
                onTagDetected?(payload)
            }
        }
    }

    func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        onTagDetected?(error.localizedDescription)
    }
}
