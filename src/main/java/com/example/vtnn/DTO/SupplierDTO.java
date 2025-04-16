package com.example.vtnn.DTO;

import com.example.vtnn.model.Supplier;

public class SupplierDTO {
    public static class SupplierRequestDTO {
        private String supplierName;
        private String contactName;
        private String address;
        private String city;
        private String postalCode;
        private String country;
        private String phone;
        private String email;

        public SupplierRequestDTO() {}

        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public String getContactName() { return contactName; }
        public void setContactName(String contactName) { this.contactName = contactName; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // DTO cho response
    public static class SupplierResponseDTO {
        private int supplierID;
        private String supplierName;
        private String contactName;
        private String address;
        private String city;
        private String postalCode;
        private String country;
        private String phone;
        private String email;

        public SupplierResponseDTO() {}

        public SupplierResponseDTO(Supplier supplier) {
            this.supplierID = supplier.getSupplierID();
            this.supplierName = supplier.getSupplierName();
            this.contactName = supplier.getContactName();
            this.address = supplier.getAddress();
            this.city = supplier.getCity();
            this.postalCode = supplier.getPostalCode();
            this.country = supplier.getCountry();
            this.phone = supplier.getPhone();
            this.email = supplier.getEmail();
        }

        public int getSupplierID() { return supplierID; }
        public void setSupplierID(int supplierID) { this.supplierID = supplierID; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public String getContactName() { return contactName; }
        public void setContactName(String contactName) { this.contactName = contactName; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
