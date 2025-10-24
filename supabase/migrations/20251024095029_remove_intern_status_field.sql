/*
  # Remove status field from interns table
  
  1. Changes
    - Drop the `status` column from `interns` table
    - This field is redundant as we use `account_status` in the `users` table instead
  
  2. Notes
    - All intern status management will now be handled through the user's account_status field
    - Valid account_status values: ACTIVE, PENDING, SUSPENDED
*/

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'interns' AND column_name = 'status'
  ) THEN
    ALTER TABLE interns DROP COLUMN status;
  END IF;
END $$;
