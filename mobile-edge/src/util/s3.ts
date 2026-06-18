
import { ENV } from "../env.js";
export async function putToS3(key: string, body: Uint8Array, contentType="application/octet-stream"){
  if (!ENV.S3_BUCKET || !ENV.S3_REGION) throw new Error("S3 not configured");
  let S3Client: any, PutObjectCommand: any;
  try {
    const sdk = await import("@aws-sdk/client-s3");
    S3Client = sdk.S3Client; PutObjectCommand = sdk.PutObjectCommand;
  } catch {
    throw new Error("aws sdk not installed");
  }
  const client = new S3Client({ region: ENV.S3_REGION });
  const cmd = new PutObjectCommand({ Bucket: ENV.S3_BUCKET, Key: key, Body: body, ContentType: contentType });
  await client.send(cmd);
  return { bucket: ENV.S3_BUCKET, key };
}
